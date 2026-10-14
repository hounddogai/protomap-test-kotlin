package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending parcel assignment is in its life cycle. */
enum class PendingParcelAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One pending parcel assignment, with what the planners need to act on it. */
data class PendingParcelAssignment(
    var code: String,
    var title: String,
    var label: String,
    var rate: Double,
    var country: String,
    var region: String,
    var description: String,
    var expiresAt: Int,
    var checksum: String,
    var contactEmail: String,
    var status: PendingParcelAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingParcelAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingParcelAssignmentStatus): Boolean {
        when (status) {
            PendingParcelAssignmentStatus.IDLE -> {
                when (next) {
                    PendingParcelAssignmentStatus.READY, PendingParcelAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            PendingParcelAssignmentStatus.WARMING -> return next == PendingParcelAssignmentStatus.READY || next == PendingParcelAssignmentStatus.DRAINING
            PendingParcelAssignmentStatus.READY -> return next == PendingParcelAssignmentStatus.DRAINING
            PendingParcelAssignmentStatus.DRAINING -> return next == PendingParcelAssignmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PendingParcelAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("pending parcel assignment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"pending parcel assignment"} $code"

    fun netTotal(): Double = round((expiresAt.toDouble() + rate) * 100) / 100

    fun snapshot(): PendingParcelAssignment {
        return PendingParcelAssignment(
            code = code,
            title = title,
            label = label,
            rate = rate,
            country = country,
            region = region,
            description = description,
            expiresAt = expiresAt,
            checksum = checksum,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending parcel assignment in status idle. */
fun newPendingParcelAssignment(code: String, title: String): PendingParcelAssignment {
    return PendingParcelAssignment(
        code = code,
        title = title,
        label = "",
        rate = 0.0,
        country = "",
        region = "",
        description = "",
        expiresAt = 0,
        checksum = "",
        contactEmail = "",
        status = PendingParcelAssignmentStatus.IDLE,
    )
}
