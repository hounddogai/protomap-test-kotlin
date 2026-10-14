package com.acme.transport.ops.freightpaysweeper

import com.acme.contracts.v1.ContractQueryServiceGrpcKt
import com.acme.contracts.v1.peekContractRequest
import com.acme.patients.v1.PatientCalculationServiceGrpcKt
import com.acme.patients.v1.getPatientRatesRequest
import com.acme.ticketing.v1.TicketServiceGrpcKt
import com.acme.ticketing.v1.deleteTicketRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.webhooks.v1.WebhookEndpointServiceGrpcKt
import com.acme.webhooks.v1.createWebhookEndpointRequest
import com.acme.webhooks.v1.webhookEndpoint
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.patients.v1.PatientCalculationService/GetPatientRates and hands what it keeps to the sink of its kind. */
suspend fun relayPatientRatesId(channel: ManagedChannel) {
    val reader = PatientCalculationServiceGrpcKt.PatientCalculationServiceCoroutineStub(channel)
    val found = reader.getPatientRates(getPatientRatesRequest { this.patientId = "current-relay-patient-rates-id" })
    val rec = RelayPatientRatesIdRecord(
        patientId = found.patient.patientId,
        status = found.result.status,
        message = found.result.message,
    )
    deliverRelayPatientRatesId("ticket", rec, channel)
}

/** Where relayPatientRatesId hands each record. */
interface RelayPatientRatesIdSink {
    suspend fun relay(rec: RelayPatientRatesIdRecord)
}

/** What relayPatientRatesId keeps of each GetPatientRates response. */
data class RelayPatientRatesIdRecord(val patientId: String, val status: String, val message: String)

/** Sends records to acme.contracts.v1.ContractQueryService. */
class RelayPatientRatesIdContractQuery(private val stub: ContractQueryServiceGrpcKt.ContractQueryServiceCoroutineStub) : RelayPatientRatesIdSink {
    override suspend fun relay(rec: RelayPatientRatesIdRecord) {
        stub.peekContract(peekContractRequest { this.contractId = rec.status })
    }
}

/** Hands records to acme.ticketing.v1.TicketService. */
class RelayPatientRatesIdTicket(private val stub: TicketServiceGrpcKt.TicketServiceCoroutineStub) : RelayPatientRatesIdSink {
    override suspend fun relay(rec: RelayPatientRatesIdRecord) {
        stub.deleteTicket(deleteTicketRequest { this.reason = rec.patientId; this.ticketId = rec.status })
    }
}

/** Sends records to acme.webhooks.v1.WebhookEndpointService. */
class RelayPatientRatesIdWebhookEndpoint(private val stub: WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub) : RelayPatientRatesIdSink {
    override suspend fun relay(rec: RelayPatientRatesIdRecord) {
        val request = createWebhookEndpointRequest {
            this.webhookEndpoint = webhookEndpoint { this.name = rec.message; this.webhookEndpointId = rec.status }
        }
        RpcBoundary.run("acme.webhooks.v1.WebhookEndpointService/CreateWebhookEndpoint") { stub.createWebhookEndpoint(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class RelayPatientRatesIdLogOnly : RelayPatientRatesIdSink {
    override suspend fun relay(rec: RelayPatientRatesIdRecord) {
        LoggerFactory.getLogger("RelayPatientRatesIdRecord").atInfo().addKeyValue("patientId", rec.patientId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRelayPatientRatesId(kind: String, rec: RelayPatientRatesIdRecord, channel: ManagedChannel) {
    var sink: RelayPatientRatesIdSink = RelayPatientRatesIdLogOnly()
    when (kind) {
        "contract-query" -> sink = RelayPatientRatesIdContractQuery(ContractQueryServiceGrpcKt.ContractQueryServiceCoroutineStub(channel))
        "ticket" -> sink = RelayPatientRatesIdTicket(TicketServiceGrpcKt.TicketServiceCoroutineStub(channel))
        "webhook-endpoint" -> sink = RelayPatientRatesIdWebhookEndpoint(WebhookEndpointServiceGrpcKt.WebhookEndpointServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
