import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.DataReaderImpl;
import com.rti.dds.subscription.DataReaderListener;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.Topic;
import com.rti.dds.topic.TypeSupportImpl;
import com.rti.dds.util.LoanableSequence;

public class SubscriberManager {
	
	private int domainId;
	private int sampleCount;
	private static DomainParticipant participant = null;
    private static Subscriber subscriber = null;
    private Topic topic;
    private DataReaderListener listener;
    private DataReaderImpl reader;
	
    private SubscriberManager(String topicName, TypeSupportImpl typeSupport, DataCallback callback) {
    	this(0,0,topicName,typeSupport,callback);
    }
    
	private SubscriberManager(int domainId, int sampleCount, String topicName, TypeSupportImpl typeSupport, DataCallback callback) {
		this.domainId = domainId;
		this.sampleCount = sampleCount;
		
		if(participant==null) 
    		createParticipant();
		if(subscriber==null)
			createSubscriber();
		
		// --- Create topic --- //

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
        
        // --- Create reader --- //

        
        listener = new TopicListener(typeSupport, callback);

        /* To customize data reader QoS, use
        the configuration file USER_QOS_PROFILES.xml */

        reader = (DataReaderImpl)
        subscriber.create_datareader(
            topic, Subscriber.DATAREADER_QOS_DEFAULT, listener,
            StatusKind.STATUS_MASK_ALL);
        if (reader == null) {
            System.err.println("create_datareader error\n");
            return;
        }      
	}
	
	public static void runSubscriberLoop() {
		 // --- Wait for data --- //

        final long receivePeriodSec = 4;

        for (;;) {
            System.out.println("TemplateTopic subscriber manager sleeping for "
            + receivePeriodSec + " sec...");

            try {
                Thread.sleep(receivePeriodSec * 1000);  // in millisec
            } catch (InterruptedException ix) {
                System.err.println("INTERRUPTED");
                break;
            }
        }
	}
	
	private void createParticipant() {
		// --- Create participant --- //

        /* To customize participant QoS, use
        the configuration file
        USER_QOS_PROFILES.xml */

        participant = DomainParticipantFactory.TheParticipantFactory.
        create_participant(
            domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
            null /* listener */, StatusKind.STATUS_MASK_NONE);
        if (participant == null) {
            System.err.println("create_participant error\n");
            return;
        }                         
	}
	
	private void createSubscriber() {
		subscriber = participant.create_subscriber(
            DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null /* listener */,
            StatusKind.STATUS_MASK_NONE);
        if (subscriber == null) {
            System.err.println("create_subscriber error\n");
            return;
        }     
	}
	
	public static interface DataCallback{
	    public void on_data_available(Object data);
	}
	
	
	private static class TopicListener extends DataReaderAdapter {
		
		private DataCallback callback;
		private LoanableSequence _dataSeq;
		private SampleInfoSeq _infoSeq;
		
		TopicListener(TypeSupportImpl typeSupport, DataCallback callback){
			super();
			this.callback = callback;
			this._dataSeq = new LoanableSequence(typeSupport.get_type());
	        this._infoSeq = new SampleInfoSeq();
		}

        public void on_data_available(DataReader r) {
            DataReaderImpl reader = (DataReaderImpl)r;

            try {
                reader.take_untyped(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);
                    if (info.valid_data) {
//                        System.out.println(
//                            ((TemplateTopic2)_dataSeq.get(i)).toString("Received",0));
                    	callback.on_data_available(_dataSeq.get(i));

                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
                reader.return_loan_untyped(_dataSeq, _infoSeq);
            }
        }
	}
	
	public static void main(String[] args) {
		
		SubscriberManager SM_message = new SubscriberManager("Example Message",MessageTypeSupport.get_instance(), new DataCallback() {
			public void on_data_available(Object data) {
				System.out.println(((Message)data).toString("Received",0));
			}
		});

        SubscriberManager SM_count = new SubscriberManager("Example Count",CountTypeSupport.get_instance(), new DataCallback() {
			public void on_data_available(Object data) {
				System.out.println(((Count)data).toString("Received",0));
			}
		});
		
		SubscriberManager.runSubscriberLoop();
	}

}
