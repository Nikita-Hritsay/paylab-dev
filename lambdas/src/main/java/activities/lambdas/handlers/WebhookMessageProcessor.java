package activities.lambdas.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

public class WebhookMessageProcessor implements RequestHandler<SQSEvent, SQSBatchResponse> {
    @Override
    public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) {
        System.out.println(sqsEvent);
        sqsEvent.getRecords().forEach(record -> {
            System.out.println(record.getBody());
            System.out.println(record.getMessageId());
        });

        return new SQSBatchResponse();
    }
}
