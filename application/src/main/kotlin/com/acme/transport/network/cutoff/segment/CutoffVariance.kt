package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.text.normalize

/** Where a cutoff variance is in its life cycle. */
enum class CutoffVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A cutoff variance. Its code is unique within a tenant. */
data class CutoffVariance(
    var code: String,
    var name: String,
    var driverId: String,
    var graceDays: Int,
    var source: String,
    var status: CutoffVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffVarianceStatus): Boolean {
        when (status) {
            CutoffVarianceStatus.OPEN -> return next == CutoffVarianceStatus.REOPENED
            CutoffVarianceStatus.IN_REVIEW -> return next == CutoffVarianceStatus.REOPENED
            CutoffVarianceStatus.RESOLVED -> {
                when (next) {
                    CutoffVarianceStatus.REOPENED -> return true
                }
                return false
            }
            CutoffVarianceStatus.REOPENED -> return next == CutoffVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CutoffVarianceStatus) {
        if (!canMoveTo(next)) {
            error("cutoff variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"cutoff variance"} $code"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): CutoffVariance {
        return CutoffVariance(
            code = code,
            name = name,
            driverId = driverId,
            graceDays = graceDays,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cutoff variance in status open. */
fun newCutoffVariance(code: String, name: String): CutoffVariance {
    return CutoffVariance(
        code = code,
        name = name,
        driverId = "",
        graceDays = 0,
        source = "",
        status = CutoffVarianceStatus.OPEN,
    )
}
