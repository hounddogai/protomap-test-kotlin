package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss draft is in its life cycle. */
enum class NearMissDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One near miss draft, with what the planners need to act on it. */
data class NearMissDraft(
    var code: String,
    var name: String,
    var retries: Int,
    var routeCode: String,
    var createdAt: Int,
    var stopCount: Int,
    var category: String,
    var expiresAt: Int,
    var vehicleId: String,
    var phone: String,
    var carrier: String,
    var status: NearMissDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissDraftStatus): Boolean {
        when (status) {
            NearMissDraftStatus.OPEN -> return next == NearMissDraftStatus.ARCHIVED
            NearMissDraftStatus.IN_REVIEW -> {
                when (next) {
                    NearMissDraftStatus.RESOLVED, NearMissDraftStatus.ARCHIVED -> return true
                }
                return false
            }
            NearMissDraftStatus.RESOLVED -> {
                when (next) {
                    NearMissDraftStatus.REOPENED, NearMissDraftStatus.ARCHIVED -> return true
                }
                return false
            }
            NearMissDraftStatus.REOPENED -> return next == NearMissDraftStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: NearMissDraftStatus) {
        if (!canMoveTo(next)) {
            error("near miss draft $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedLoad(): Double = round((createdAt.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): NearMissDraft {
        return NearMissDraft(
            code = code,
            name = name,
            retries = retries,
            routeCode = routeCode,
            createdAt = createdAt,
            stopCount = stopCount,
            category = category,
            expiresAt = expiresAt,
            vehicleId = vehicleId,
            phone = phone,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a near miss draft in status open. */
fun newNearMissDraft(code: String, name: String): NearMissDraft {
    return NearMissDraft(
        code = code,
        name = name,
        retries = 0,
        routeCode = "",
        createdAt = 0,
        stopCount = 0,
        category = "",
        expiresAt = 0,
        vehicleId = "",
        phone = "",
        carrier = "",
        status = NearMissDraftStatus.OPEN,
    )
}
