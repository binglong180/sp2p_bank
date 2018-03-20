package payment.newr.zj.service;

import payment.api.tx.marketorder.Tx1341Request;
import payment.api.tx.marketorder.Tx134xResponse;
import payment.api.tx.marketorder.Tx1375Request;
import payment.api.tx.marketorder.Tx1375Response;
import payment.api.tx.marketorder.Tx1376Request;
import payment.api.tx.marketorder.Tx1376Response;

public interface PaymentService {
	public Tx1375Response investSMS(Tx1375Request tx1375Request);
	public Tx1376Response investSMSVerify(Tx1376Request tx1376Request);
	public Tx134xResponse fullBidRelease( Tx1341Request  tx1341Request);
}
