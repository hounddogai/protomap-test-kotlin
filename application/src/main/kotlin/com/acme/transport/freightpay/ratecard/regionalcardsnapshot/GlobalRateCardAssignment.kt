package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global rate card assignment is in its life cycle. */
enum class GlobalRateCardAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global rate card assignment. Its code is unique within a tenant. */
data class GlobalRateCardAssignment(
    var code: String,
    var name: String,
    var title: String,
    var ratio: Double,
    var capacity: Int,
    var externalRef: String,
    var durationMinutes: Int,
    var sequence: Int,
    var archived: Boolean,
    var endsAt: Int,
    var status: GlobalRateCardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRateCardAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRateCardAssignmentStatus): Boolean {
        when (status) {
            GlobalRateCardAssignmentStatus.OPEN -> {
                when (next) {
                    GlobalRateCardAssignmentStatus.IN_REVIEW, GlobalRateCardAssignmentStatus.REOPENED -> return true
                }
                return false
            }
            GlobalRateCardAssignmentStatus.IN_REVIEW -> {
                when (next) {
                    GlobalRateCardAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalRateCardAssignmentStatus.RESOLVED -> return next == GlobalRateCardAssignmentStatus.REOPENED || next == GlobalRateCardAssignmentStatus.ARCHIVED
            GlobalRateCardAssignmentStatus.REOPENED -> return next == GlobalRateCardAssignmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalRateCardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("global rate card assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((endsAt.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): GlobalRateCardAssignment {
        return GlobalRateCardAssignment(
            code = code,
            name = name,
            title = title,
            ratio = ratio,
            capacity = capacity,
            externalRef = externalRef,
            durationMinutes = durationMinutes,
            sequence = sequence,
            archived = archived,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global rate card assignment in status open. */
fun newGlobalRateCardAssignment(code: String, name: String): GlobalRateCardAssignment {
    return GlobalRateCardAssignment(
        code = code,
        name = name,
        title = "",
        ratio = 0.0,
        capacity = 0,
        externalRef = "",
        durationMinutes = 0,
        sequence = 0,
        archived = false,
        endsAt = 0,
        status = GlobalRateCardAssignmentStatus.OPEN,
    )
}
