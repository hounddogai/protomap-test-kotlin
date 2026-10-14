package com.acme.transport.ops.freightpaysweeper

import com.acme.recruiting.v1.JobOpeningServiceGrpcKt
import com.acme.recruiting.v1.deleteJobOpeningRequest
import com.acme.transport.kit.text.normalize
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.submitDockRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.DockApprovalService/SubmitDock and hands what it keeps to acme.recruiting.v1.JobOpeningService. */
suspend fun mirrorDockStatus(channel: ManagedChannel) {
    val origin = DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub(channel)
    val current = origin.submitDock(submitDockRequest { this.reason = "primary-mirror-dock-status" })
    val entry = MirrorDockStatusRecord(
        status = current.outcome.status,
        resultStatus = normalize(current.result.status),
    )
    MirrorDockStatusJobOpening(JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub(channel)).forward(entry)
}

/** What mirrorDockStatus keeps of each SubmitDock response. */
data class MirrorDockStatusRecord(val status: String, val resultStatus: String)

/** Passes records to acme.recruiting.v1.JobOpeningService. */
class MirrorDockStatusJobOpening(private val stub: JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub) {
    suspend fun forward(rec: MirrorDockStatusRecord) {
        stub.deleteJobOpening(deleteJobOpeningRequest { this.reason = rec.status; this.jobOpeningId = rec.resultStatus })
    }
}
