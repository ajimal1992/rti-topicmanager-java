import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.DataWriterImpl;
import com.rti.dds.publication.Publisher;
import com.rti.dds.topic.Topic;
import com.rti.dds.topic.TypeSupportImpl;

public class PublisherManager {
	
	// -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------

    // --- Constructors: -----------------------------------------------------
	
	private int domainId;
	private int sampleCount;
    private static DomainParticipant participant = null;
    private static Publisher publisher = null;
    private Topic topic;
    private DataWriterImpl writer;
    //Object instance;
    private InstanceHandle_t instance_handle;
    
    private PublisherManager(String topicName, TypeSupportImpl typeSupport) {
    	this(0,0,topicName,typeSupport);
    }
    
    private PublisherManager(int domainId, int sampleCount, String topicName, TypeSupportImpl typeSupport) {
    	this.domainId = domainId;
    	this.sampleCount = sampleCount;
    	
    	if(participant==null) 
    		createParticipant();
    	if(publisher ==null)
    		createPublisher();

        //---Create topic---//
        /* Register type before creating topic */
        String typeName = typeSupport.get_type_nameI();
        typeSupport.register_typeI(participant, typeName);

        /* To customize topic QoS, use
        the configuration file USER_QOS_PROFILES.xml */

        topic = participant.create_topic(
            topicName,
            typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
            null /* listener */, StatusKind.STATUS_MASK_NONE);
        if (topic == null) {
            System.err.println("create_topic error\n");
            return;
        }           
        
        
        //---Create Writer---// //TODO: Currently using the default implementations of datawriters. Maybe try to use the object specifc writers. Eg, TemplateTopic2DataWriter
        writer = 
	        (DataWriterImpl) publisher.create_datawriter(
	            topic, Publisher.DATAWRITER_QOS_DEFAULT,
	            null /* listener */, StatusKind.STATUS_MASK_NONE);
        if (writer == null) {
            System.err.println("create_datawriter error\n");
            return;
        }   
        
        // --- Write --- //

        /* Create data sample for writing */
        //this.instance = instance;
        instance_handle = InstanceHandle_t.HANDLE_NIL;
        /* For a data type that has a key, if the same instance is going to be
        written multiple times, initialize the key here
        and register the keyed instance prior to writing */
        //instance_handle = writer.register_instance(instance);
        
        
        //writer.write_untyped(this.instance, instance_handle);
    }
    
    public void writeInstance(Object instance) {
    	writer.write_untyped(instance, this.instance_handle);
    }
    
    private void createParticipant() {
    	
    	participant = DomainParticipantFactory.TheParticipantFactory.
            create_participant(
                domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }   
    }
    
    private void createPublisher() {
    	publisher = participant.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }  
    }
    
    public static void main(String[] args) {
    	PublisherManager PM_message = new PublisherManager("Example Message",MessageTypeSupport.get_instance());
    	PublisherManager PM_count = new PublisherManager("Example Count",CountTypeSupport.get_instance());
    	
    	/* Create data sample for writing */
        Message message_instance = new Message();
        Count count_instance = new Count();
        
        final long sendPeriodMillis = 4 * 1000; // 4 seconds

        int counter = 1;
        for (;;) {
            System.out.println("Writing with PublisherManager, count " + counter);

            /* Modify the instance to be written here */
            message_instance.msg = "Helloworld!";
            count_instance.count = counter;
            
            /* Write data */
            PM_message.writeInstance(message_instance);
            PM_count.writeInstance(count_instance);
            counter++;
            try {
                Thread.sleep(sendPeriodMillis);
            } catch (InterruptedException ix) {
                System.err.println("INTERRUPTED");
                break;
            }
        }
    }
    
    
}
