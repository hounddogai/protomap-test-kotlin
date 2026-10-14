package com.acme.transport.ops.customsauditor

import com.acme.certificates.v1.CertificateServiceGrpcKt
import com.acme.certificates.v1.updateCertificateRequest
import com.acme.checkout.v1.CheckoutSessionQueryServiceGrpcKt
import com.acme.checkout.v1.lookupCheckoutSessionRequest
import com.acme.customers.v1.CustomerApprovalServiceGrpcKt
import com.acme.customers.v1.customer
import com.acme.customers.v1.delegateCustomerApprovalRequest
import com.acme.fuel.v1.CancelFuelTransactionImportRequestKt
import com.acme.fuel.v1.FuelTransactionImportServiceGrpcKt
import com.acme.fuel.v1.cancelFuelTransactionImportRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.approveYardMoveRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.certificates.v1.CertificateService/UpdateCertificate and hands what it keeps to the sink of its kind. */
suspend fun carryCertificateMessage(channel: ManagedChannel) {
    val upstream = CertificateServiceGrpcKt.CertificateServiceCoroutineStub(channel)
    val got = RpcBoundary.read { upstream.updateCertificate(updateCertificateRequest { }) }
    val rec = CarryCertificateMessageRecord(
        message = got.result.message,
        correlationId = got.result.correlationId,
    )
    deliverCarryCertificateMessage("fuel-transaction-import", rec, channel)
}

/** Where carryCertificateMessage hands each record. */
interface CarryCertificateMessageDestination {
    suspend fun transmit(rec: CarryCertificateMessageRecord)
}

/** What carryCertificateMessage keeps of each UpdateCertificate response. */
data class CarryCertificateMessageRecord(val message: String, val correlationId: String)

/** Passes records to acme.fuel.v1.FuelTransactionImportService. */
class CarryCertificateMessageFuelTransactionImport(private val stub: FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub) : CarryCertificateMessageDestination {
    override suspend fun transmit(rec: CarryCertificateMessageRecord) {
        val request = cancelFuelTransactionImportRequest {
            this.details = CancelFuelTransactionImportRequestKt.details { this.reasonCode = rec.correlationId }
            this.fuelTransactionId = rec.message
        }
        stub.cancelFuelTransactionImport(request)
    }
}

/** Writes records to acme.checkout.v1.CheckoutSessionQueryService. */
class CarryCertificateMessageCheckoutSessionQuery(private val stub: CheckoutSessionQueryServiceGrpcKt.CheckoutSessionQueryServiceCoroutineStub) : CarryCertificateMessageDestination {
    override suspend fun transmit(rec: CarryCertificateMessageRecord) {
        stub.lookupCheckoutSession(lookupCheckoutSessionRequest { this.checkoutSessionId = rec.message })
    }
}

/** Passes records to acme.yards.v1.YardMoveApprovalService. */
class CarryCertificateMessageYardMoveApproval(private val stub: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub) : CarryCertificateMessageDestination {
    override suspend fun transmit(rec: CarryCertificateMessageRecord) {
        stub.approveYardMove(approveYardMoveRequest { this.yardMoveId = rec.correlationId })
    }
}

/** Sends records to acme.customers.v1.CustomerApprovalService. */
class CarryCertificateMessageCustomerApproval(private val stub: CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub) : CarryCertificateMessageDestination {
    override suspend fun transmit(rec: CarryCertificateMessageRecord) {
        val request = delegateCustomerApprovalRequest {
            this.customer = customer { this.customerId = rec.message }
        }
        stub.delegateCustomerApproval(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class CarryCertificateMessageLogOnly : CarryCertificateMessageDestination {
    override suspend fun transmit(rec: CarryCertificateMessageRecord) {
        LoggerFactory.getLogger("CarryCertificateMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverCarryCertificateMessage(kind: String, rec: CarryCertificateMessageRecord, channel: ManagedChannel) {
    var sink: CarryCertificateMessageDestination = CarryCertificateMessageLogOnly()
    when (kind) {
        "fuel-transaction-import" -> sink = CarryCertificateMessageFuelTransactionImport(FuelTransactionImportServiceGrpcKt.FuelTransactionImportServiceCoroutineStub(channel))
        "checkout-session-query" -> sink = CarryCertificateMessageCheckoutSessionQuery(CheckoutSessionQueryServiceGrpcKt.CheckoutSessionQueryServiceCoroutineStub(channel))
        "yard-move-approval" -> sink = CarryCertificateMessageYardMoveApproval(YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub(channel))
        "customer-approval" -> sink = CarryCertificateMessageCustomerApproval(CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
