package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return baseline is in its life cycle. */
enum class LeaseReturnBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lease return baseline. Its code is unique within a tenant. */
data class LeaseReturnBaseline(
    var code: String,
    var label: String,
    var threshold: Double,
    var etaMinutes: Int,
    var country: String,
    var endsAt: Int,
    var status: LeaseReturnBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnBaselineStatus): Boolean {
        when (status) {
            LeaseReturnBaselineStatus.NEW -> {
                when (next) {
                    LeaseReturnBaselineStatus.QUEUED -> return true
                }
                return false
            }
            LeaseReturnBaselineStatus.QUEUED -> return next == LeaseReturnBaselineStatus.FAILED
            LeaseReturnBaselineStatus.RUNNING -> {
                when (next) {
                    LeaseReturnBaselineStatus.FAILED -> return true
                }
                return false
            }
            LeaseReturnBaselineStatus.DONE -> return next == LeaseReturnBaselineStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LeaseReturnBaselineStatus) {
        if (!canMoveTo(next)) {
            error("lease return baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netExposure(): Double = round((endsAt.toDouble() - threshold) * 100) / 100

    fun snapshot(): LeaseReturnBaseline {
        return LeaseReturnBaseline(
            code = code,
            label = label,
            threshold = threshold,
            etaMinutes = etaMinutes,
            country = country,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lease return baseline in status new. */
fun newLeaseReturnBaseline(code: String, label: String): LeaseReturnBaseline {
    return LeaseReturnBaseline(
        code = code,
        label = label,
        threshold = 0.0,
        etaMinutes = 0,
        country = "",
        endsAt = 0,
        status = LeaseReturnBaselineStatus.NEW,
    )
}
