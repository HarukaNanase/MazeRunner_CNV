import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;


public class DynamoController {
    private static String TABLE_NAME = "metrics";
    private static String REGION = "us-east-1";
    private static boolean started = false;
    static AmazonDynamoDB dynamoDB;


    public static void init() throws Exception{
        if(started)
            return;
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(REGION)
                .build();
        try {
            //Table table = new Table(dynamoDB, TABLE_NAME);
            //DeleteTableResult del = table.delete();
            //System.out.println("DynamoController: Waiting to delete already existing table.");
            //table.waitForDelete();
        }catch(ResourceNotFoundException rnfe){

            System.out.println("DynamoController: Table not found... Creating it now!");
        }
        CreateTable();
        started = true;
    }

    public static void CreateTable(String... tablename) throws Exception{
        System.out.println("DynamoController: Creating table with name: " + TABLE_NAME);
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(TABLE_NAME)
                .withKeySchema(new KeySchemaElement().withAttributeName("UUID").withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName("UUID").withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
        // wait for the table to move into ACTIVE state
        try {
            TableUtils.waitUntilActive(dynamoDB, TABLE_NAME);
        }catch(AmazonServiceException ase){
            handleServiceException(ase);
        }catch(AmazonClientException ace){
            handleClientException(ace);
        }
        DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(TABLE_NAME);
        TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
        System.out.println("Table Description: " + tableDescription);
    }


    private static void handleServiceException(AmazonServiceException ase){
        System.out.println("Caught an AmazonServiceException, which means your request made it "
                + "to AWS, but was rejected with an error response for some reason.");
        System.out.println("Error Message:    " + ase.getMessage());
        System.out.println("HTTP Status Code: " + ase.getStatusCode());
        System.out.println("AWS Error Code:   " + ase.getErrorCode());
        System.out.println("Error Type:       " + ase.getErrorType());
        System.out.println("Request ID:       " + ase.getRequestId());
    }

    private static void handleClientException(AmazonClientException ace){
        System.out.println("Caught an AmazonClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with AWS, "
                + "such as not being able to access the network.");
        System.out.println("Error Message: " + ace.getMessage());
    }

}
