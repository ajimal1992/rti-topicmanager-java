# rti-topicmanager-java
RTI helper class and scripts to build RTI applications.

### Setup
Ensure RTI License File is installed.
Ensure that `NDDSHOME`,`PATH`,`LD_LIBRARY_PATH` variables are set. Eg,

    export NDDSHOME=/home/user/rti_connext_dds-6.0.0
    export PATH=$PATH:/home/user/rti_connext_dds-6.0.0/bin
    export LD_LIBRARY_PATH=/home/user/rti_connext_dds-6.0.0/lib/x64Linux3gcc5.4.0
### Build and run RTI application
1. Clone repo.

       git clone https://github.com/ajimal1992/rti-topicmanager-java.git
2. cd to repo.

       cd rti-topicmanager-java
3. Generate RTI codes based on Sample IDL file.

       ./gencode.sh SampleIDL.idl
4. Build source codes.

       ./build.sh
5. Run Publisher.

       ./runPub.sh
6. Open another terminal and run Subscriber.

       ./runSub.sh
### Usage - PublisherManager.java

Create PublisherManager for Message type

    PublisherManager PM_message = new PublisherManager("Example Message",MessageTypeSupport.get_instance());

Create data instance for Message type

    Message message_instance = new Message();
    
Modify data instance for Message type

    message_instance.msg = "Helloworld!";
    
Write data instance for Message type

    PM_message.writeInstance(message_instance);
### Usage - SubscriberManager.java

Create SubscriberManager and process on data available for Message type

    SubscriberManager SM_message = new SubscriberManager("Example Message",MessageTypeSupport.get_instance(), new DataCallback() {
        public void on_data_available(Object data) {
          System.out.println(((Message)data).toString("Received",0));
        }
    });

Run Subscriber loop. The loop should only be started at the bottom of your `main` method.

    SubscriberManager.runSubscriberLoop();



