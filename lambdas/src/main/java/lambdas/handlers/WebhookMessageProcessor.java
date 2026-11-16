package lambdas.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

public class WebhookMessageProcessor implements RequestHandler<SQSEvent, SQSBatchResponse> {
    @Override
    public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) {
        SQSBatchResponse  sqsBatchResponse = new SQSBatchResponse();
        if (sqsEvent != null && !sqsEvent.getRecords().isEmpty()) {
            sqsEvent.getRecords().forEach(record -> {
                System.out.println("Received a message " + record.getMessageId() + " and body " + record.getBody());
            });
        }
        else {
            System.out.println("Event is null or has no records in it");
        }
        return sqsBatchResponse;
    }
}
