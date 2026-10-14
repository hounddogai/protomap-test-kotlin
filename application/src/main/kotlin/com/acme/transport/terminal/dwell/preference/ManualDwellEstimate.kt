package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual dwell estimate is in its life cycle. */
enum class ManualDwellEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual dwell estimate, with what the planners need to act on it. */
data class ManualDwellEstimate(
    var code: String,
    var name: String,
    var capacity: Int,
    var score: Double,
    var trailerId: String,
    var contactEmail: String,
    var status: ManualDwellEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDwellEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDwellEstimateStatus): Boolean {
        when (status) {
            ManualDwellEstimateStatus.DRAFT -> {
                when (next) {
                    ManualDwellEstimateStatus.CLOSED -> return true
                }
                return false
            }
            ManualDwellEstimateStatus.PENDING -> return next == ManualDwellEstimateStatus.ACTIVE
            ManualDwellEstimateStatus.ACTIVE -> return next == ManualDwellEstimateStatus.SUSPENDED || next == ManualDwellEstimateStatus.CLOSED
            ManualDwellEstimateStatus.SUSPENDED -> {
                when (next) {
                    ManualDwellEstimateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDwellEstimateStatus) {
        if (!canMoveTo(next)) {
            error("manual dwell estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedLoad(): Double = round((score - capacity.toDouble()) * 100) / 100

    fun snapshot(): ManualDwellEstimate {
        return ManualDwellEstimate(
            code = code,
            name = name,
            capacity = capacity,
            score = score,
            trailerId = trailerId,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual dwell estimate in status draft. */
fun newManualDwellEstimate(code: String, name: String): ManualDwellEstimate {
    return ManualDwellEstimate(
        code = code,
        name = name,
        capacity = 0,
        score = 0.0,
        trailerId = "",
        contactEmail = "",
        status = ManualDwellEstimateStatus.DRAFT,
    )
}
