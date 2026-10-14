package com.acme.transport.ops.terminalsweeper

import com.acme.common.v1.pageRequest
import com.acme.configuration.v1.ConfigSetCalculationServiceGrpcKt
import com.acme.configuration.v1.simulateConfigSetRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.deleteDocumentRequest
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.driverHome
import com.acme.driverapp.v1.recallDriverHomeRequest
import com.acme.performers.v1.ListPerformersRequestKt
import com.acme.performers.v1.PerformerServiceGrpcKt
import com.acme.performers.v1.listPerformersRequest
import io.grpc.ManagedChannel

/** Reads acme.configuration.v1.ConfigSetCalculationService/SimulateConfigSet and hands what it keeps to the sink of its kind. */
suspend fun forwardConfigSetId(channel: ManagedChannel) {
    val origin = ConfigSetCalculationServiceGrpcKt.ConfigSetCalculationServiceCoroutineStub(channel)
    val reply = origin.simulateConfigSet(simulateConfigSetRequest { this.configSetId = "current-forward-config-set-id" })
    val rec = ForwardConfigSetIdRecord(
        correlationId = reply.result.correlationId,
        message = reply.result.message,
        status = reply.result.status,
        configSetId = reply.configSet.configSetId,
    )
    val sinks = listOf<ForwardConfigSetIdTarget>(
        ForwardConfigSetIdDriverHomeApproval(DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub(channel)),
        ForwardConfigSetIdPerformer(PerformerServiceGrpcKt.PerformerServiceCoroutineStub(channel)),
        ForwardConfigSetIdDocument(DocumentServiceGrpcKt.DocumentServiceCoroutineStub(channel)),
    )
    sinks[0].deliver(rec)
}

/** Where forwardConfigSetId hands each record. */
interface ForwardConfigSetIdTarget {
    suspend fun deliver(rec: ForwardConfigSetIdRecord)
}

/** What forwardConfigSetId keeps of each SimulateConfigSet response. */
data class ForwardConfigSetIdRecord(val correlationId: String, val message: String, val status: String, val configSetId: String)

/** Sends records to acme.driverapp.v1.DriverHomeApprovalService. */
class ForwardConfigSetIdDriverHomeApproval(private val stub: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub) : ForwardConfigSetIdTarget {
    override suspend fun deliver(rec: ForwardConfigSetIdRecord) {
        val request = recallDriverHomeRequest {
            this.driverHome = driverHome { this.driverHomeId = rec.correlationId }
            this.driverHomeId = rec.message
            this.reason = rec.status
        }
        stub.recallDriverHome(request)
    }
}

/** Sends records to acme.performers.v1.PerformerService. */
class ForwardConfigSetIdPerformer(private val stub: PerformerServiceGrpcKt.PerformerServiceCoroutineStub) : ForwardConfigSetIdTarget {
    override suspend fun deliver(rec: ForwardConfigSetIdRecord) {
        val request = listPerformersRequest {
            this.filter = ListPerformersRequestKt.filter { this.ownerId = rec.status; this.region = rec.configSetId }
            this.page = pageRequest { this.pageToken = rec.message }
        }
        stub.listPerformers(request)
    }
}

/** Passes records to acme.documents.v1.DocumentService. */
class ForwardConfigSetIdDocument(private val stub: DocumentServiceGrpcKt.DocumentServiceCoroutineStub) : ForwardConfigSetIdTarget {
    override suspend fun deliver(rec: ForwardConfigSetIdRecord) {
        val request = deleteDocumentRequest {
            this.documentId = rec.status
            this.reason = rec.configSetId
            this.etag = rec.message
        }
        stub.deleteDocument(request)
    }
}
