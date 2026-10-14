package com.acme.transport.ops.terminalsweeper

import com.acme.fleet.v1.VehicleApprovalServiceGrpcKt
import com.acme.fleet.v1.escalateVehicleRequest
import com.acme.lending.v1.LoanApplicationQueryServiceGrpcKt
import com.acme.lending.v1.SuggestLoanApplicationsRequestKt
import com.acme.lending.v1.suggestLoanApplicationsRequest
import com.acme.maintenance.v1.EscalateServiceScheduleRequestKt
import com.acme.maintenance.v1.ServiceScheduleApprovalServiceGrpcKt
import com.acme.maintenance.v1.escalateServiceScheduleRequest
import com.acme.maintenance.v1.serviceSchedule
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fleet.v1.VehicleApprovalService/EscalateVehicle and hands what it keeps to the sink of its kind. */
suspend fun mirrorVehicleCode(channel: ManagedChannel) {
    val source = VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineStub(channel)
    val reply = source.escalateVehicle(escalateVehicleRequest { this.vehicleId = "current-mirror-vehicle-code" })
    val entry = MirrorVehicleCodeRecord(
        code = reply.vehicle.code,
        vehicleId = normalize(reply.vehicle.vehicleId),
        name = normalize(reply.vehicle.name),
        status = reply.outcome.status,
    )
    deliverMirrorVehicleCode("schedule-approval", entry, channel)
}

/** Where mirrorVehicleCode hands each record. */
interface MirrorVehicleCodeDestination {
    suspend fun write(rec: MirrorVehicleCodeRecord)
}

/** What mirrorVehicleCode keeps of each EscalateVehicle response. */
data class MirrorVehicleCodeRecord(val code: String, val vehicleId: String, val name: String, val status: String)

/** Sends records to acme.lending.v1.LoanApplicationQueryService. */
class MirrorVehicleCodeLoanApplicationQuery(private val stub: LoanApplicationQueryServiceGrpcKt.LoanApplicationQueryServiceCoroutineStub) : MirrorVehicleCodeDestination {
    override suspend fun write(rec: MirrorVehicleCodeRecord) {
        val request = suggestLoanApplicationsRequest {
            this.filter = SuggestLoanApplicationsRequestKt.filter { this.ownerId = rec.code; this.region = rec.name }
        }
        stub.suggestLoanApplications(request)
    }
}

/** Sends records to acme.maintenance.v1.ServiceScheduleApprovalService. */
class MirrorVehicleCodeServiceScheduleApproval(private val stub: ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub) : MirrorVehicleCodeDestination {
    override suspend fun write(rec: MirrorVehicleCodeRecord) {
        val request = escalateServiceScheduleRequest {
            this.details = EscalateServiceScheduleRequestKt.details { this.approverEmail = rec.vehicleId; this.comment = rec.status }
            this.serviceSchedule = serviceSchedule { this.title = rec.code }
        }
        stub.escalateServiceSchedule(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorVehicleCodeLogOnly : MirrorVehicleCodeDestination {
    override suspend fun write(rec: MirrorVehicleCodeRecord) {
        LoggerFactory.getLogger("MirrorVehicleCodeRecord").atInfo().addKeyValue("code", rec.code).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverMirrorVehicleCode(kind: String, rec: MirrorVehicleCodeRecord, channel: ManagedChannel) {
    var sink: MirrorVehicleCodeDestination = MirrorVehicleCodeLogOnly()
    when (kind) {
        "loan-application-query" -> sink = MirrorVehicleCodeLoanApplicationQuery(LoanApplicationQueryServiceGrpcKt.LoanApplicationQueryServiceCoroutineStub(channel))
        "schedule-approval" -> sink = MirrorVehicleCodeServiceScheduleApproval(ServiceScheduleApprovalServiceGrpcKt.ServiceScheduleApprovalServiceCoroutineStub(channel))
    }
    sink.write(rec)
}
