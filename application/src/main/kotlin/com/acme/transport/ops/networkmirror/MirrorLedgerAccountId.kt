package com.acme.transport.ops.networkmirror

import com.acme.common.v1.pageRequest
import com.acme.deliveries.v1.ProofOfDeliveryWorkflowServiceGrpcKt
import com.acme.deliveries.v1.pauseProofOfDeliveryWorkflowRequest
import com.acme.deliveries.v1.proofOfDelivery
import com.acme.driverapp.v1.DriverInboxNotificationServiceGrpcKt
import com.acme.driverapp.v1.DriverInboxWorkflowServiceGrpcKt
import com.acme.driverapp.v1.driverInbox
import com.acme.driverapp.v1.listDriverInboxSubscriptionsRequest
import com.acme.driverapp.v1.retryDriverInboxWorkflowRequest
import com.acme.fuel.v1.FuelTransactionValidationServiceGrpcKt
import com.acme.fuel.v1.explainFuelTransactionDecisionRequest
import com.acme.fuel.v1.fuelTransaction
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.getLedgerAccountRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.ledger.v1.LedgerAccountService/GetLedgerAccount and hands what it keeps to the sink of its kind. */
suspend fun mirrorLedgerAccountId(channel: ManagedChannel) {
    val reader = LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub(channel)
    val found = reader.getLedgerAccount(getLedgerAccountRequest { this.ledgerAccountId = "primary-mirror-ledger-account-id" })
    val record = MirrorLedgerAccountIdRecord(
        correlationId = normalize(found.result.correlationId),
        ledgerAccountId = normalize(found.ledgerAccount.ledgerAccountId),
    )
    for (sink in listOf<MirrorLedgerAccountIdDestination>(
        MirrorLedgerAccountIdFuelTransactionValidation(FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub(channel)),
        MirrorLedgerAccountIdProofOfDeliveryWorkflow(ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub(channel)),
        MirrorLedgerAccountIdDriverInboxWorkflow(DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub(channel)),
        MirrorLedgerAccountIdDriverInboxNotification(DriverInboxNotificationServiceGrpcKt.DriverInboxNotificationServiceCoroutineStub(channel)),
    )) {
        sink.transmit(record)
    }
}

/** Where mirrorLedgerAccountId hands each record. */
interface MirrorLedgerAccountIdDestination {
    suspend fun transmit(rec: MirrorLedgerAccountIdRecord)
}

/** What mirrorLedgerAccountId keeps of each GetLedgerAccount response. */
data class MirrorLedgerAccountIdRecord(val correlationId: String, val ledgerAccountId: String)

/** Hands records to acme.fuel.v1.FuelTransactionValidationService. */
class MirrorLedgerAccountIdFuelTransactionValidation(private val stub: FuelTransactionValidationServiceGrpcKt.FuelTransactionValidationServiceCoroutineStub) : MirrorLedgerAccountIdDestination {
    override suspend fun transmit(rec: MirrorLedgerAccountIdRecord) {
        val request = explainFuelTransactionDecisionRequest {
            this.fuelTransaction = fuelTransaction { this.kind = rec.correlationId; this.source = rec.ledgerAccountId }
        }
        RpcBoundary.run("acme.fuel.v1.FuelTransactionValidationService/ExplainFuelTransactionDecision") { stub.explainFuelTransactionDecision(request) }
    }
}

/** Writes records to acme.deliveries.v1.ProofOfDeliveryWorkflowService. */
class MirrorLedgerAccountIdProofOfDeliveryWorkflow(private val stub: ProofOfDeliveryWorkflowServiceGrpcKt.ProofOfDeliveryWorkflowServiceCoroutineStub) : MirrorLedgerAccountIdDestination {
    override suspend fun transmit(rec: MirrorLedgerAccountIdRecord) {
        val request = pauseProofOfDeliveryWorkflowRequest {
            this.reason = rec.ledgerAccountId
            this.proofOfDelivery = proofOfDelivery { this.proofOfDeliveryId = rec.correlationId }
        }
        stub.pauseProofOfDeliveryWorkflow(request)
    }
}

/** Hands records to acme.driverapp.v1.DriverInboxWorkflowService. */
class MirrorLedgerAccountIdDriverInboxWorkflow(private val stub: DriverInboxWorkflowServiceGrpcKt.DriverInboxWorkflowServiceCoroutineStub) : MirrorLedgerAccountIdDestination {
    override suspend fun transmit(rec: MirrorLedgerAccountIdRecord) {
        val request = retryDriverInboxWorkflowRequest {
            this.driverInbox = driverInbox { this.number = rec.ledgerAccountId }
        }
        stub.retryDriverInboxWorkflow(request)
    }
}

/** Writes records to acme.driverapp.v1.DriverInboxNotificationService. */
class MirrorLedgerAccountIdDriverInboxNotification(private val stub: DriverInboxNotificationServiceGrpcKt.DriverInboxNotificationServiceCoroutineStub) : MirrorLedgerAccountIdDestination {
    override suspend fun transmit(rec: MirrorLedgerAccountIdRecord) {
        val request = listDriverInboxSubscriptionsRequest {
            this.page = pageRequest { this.pageToken = rec.ledgerAccountId }
        }
        stub.listDriverInboxSubscriptions(request)
    }
}
