package com.acme.transport.ops.fleetopsreconciler

import com.acme.customers.v1.CustomerQueryServiceGrpcKt
import com.acme.customers.v1.CustomerServiceGrpcKt
import com.acme.customers.v1.cloneCustomerRequest
import com.acme.customers.v1.countCustomersRequest
import com.acme.customers.v1.customer
import com.acme.secrets.v1.FindSecretsRequestKt
import com.acme.secrets.v1.SecretQueryServiceGrpcKt
import com.acme.secrets.v1.findSecretsRequest
import com.acme.telehealth.v1.VideoVisitCalculationServiceGrpcKt
import com.acme.telehealth.v1.getVideoVisitRatesRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.customers.v1.CustomerQueryService/CountCustomers and hands what it keeps to the sink of its kind. */
suspend fun copyCustomersStatus(channel: ManagedChannel) {
    val source = CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub(channel)
    val found = source.countCustomers(countCustomersRequest { this.orderBy = "live-copy-customers-status" })
    val entry = CopyCustomersStatusRecord(
        status = found.result.status,
        correlationId = found.result.correlationId,
    )
    deliverCopyCustomersStatus("video-visit-calculation", entry, channel)
}

/** Where copyCustomersStatus hands each record. */
interface CopyCustomersStatusWriter {
    suspend fun forward(rec: CopyCustomersStatusRecord)
}

/** What copyCustomersStatus keeps of each CountCustomers response. */
data class CopyCustomersStatusRecord(val status: String, val correlationId: String)

/** Writes records to acme.telehealth.v1.VideoVisitCalculationService. */
class CopyCustomersStatusVideoVisitCalculation(private val stub: VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub) : CopyCustomersStatusWriter {
    override suspend fun forward(rec: CopyCustomersStatusRecord) {
        stub.getVideoVisitRates(getVideoVisitRatesRequest { this.videoVisitId = rec.correlationId })
    }
}

/** Sends records to acme.secrets.v1.SecretQueryService. */
class CopyCustomersStatusSecretQuery(private val stub: SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub) : CopyCustomersStatusWriter {
    override suspend fun forward(rec: CopyCustomersStatusRecord) {
        val request = findSecretsRequest {
            this.filter = FindSecretsRequestKt.filter { this.ownerId = rec.status; this.query = rec.correlationId }
        }
        stub.findSecrets(request)
    }
}

/** Sends records to acme.customers.v1.CustomerService. */
class CopyCustomersStatusCustomer(private val stub: CustomerServiceGrpcKt.CustomerServiceCoroutineStub) : CopyCustomersStatusWriter {
    override suspend fun forward(rec: CopyCustomersStatusRecord) {
        RpcBoundary.run("acme.customers.v1.CustomerService/CloneCustomer") { stub.cloneCustomer(cloneCustomerRequest { this.customer = customer { this.customerId = rec.status } }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class CopyCustomersStatusLogOnly : CopyCustomersStatusWriter {
    override suspend fun forward(rec: CopyCustomersStatusRecord) {
        LoggerFactory.getLogger("CopyCustomersStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverCopyCustomersStatus(kind: String, rec: CopyCustomersStatusRecord, channel: ManagedChannel) {
    var sink: CopyCustomersStatusWriter = CopyCustomersStatusLogOnly()
    when (kind) {
        "video-visit-calculation" -> sink = CopyCustomersStatusVideoVisitCalculation(VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub(channel))
        "secret-query" -> sink = CopyCustomersStatusSecretQuery(SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub(channel))
        "customer" -> sink = CopyCustomersStatusCustomer(CustomerServiceGrpcKt.CustomerServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
