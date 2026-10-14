package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.text.normalize

/** Where a global cross dock variance is in its life cycle. */
enum class GlobalCrossDockVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global cross dock variance. Its code is unique within a tenant. */
data class GlobalCrossDockVariance(
    var code: String,
    var title: String,
    var active: Boolean,
    var expiresAt: Int,
    var depot: String,
    var status: GlobalCrossDockVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCrossDockVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCrossDockVarianceStatus): Boolean {
        when (status) {
            GlobalCrossDockVarianceStatus.PLANNED -> {
                when (next) {
                    GlobalCrossDockVarianceStatus.SCHEDULED, GlobalCrossDockVarianceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            GlobalCrossDockVarianceStatus.SCHEDULED -> return next == GlobalCrossDockVarianceStatus.COMPLETED
            GlobalCrossDockVarianceStatus.IN_PROGRESS -> return next == GlobalCrossDockVarianceStatus.COMPLETED
            GlobalCrossDockVarianceStatus.COMPLETED -> return next == GlobalCrossDockVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalCrossDockVarianceStatus) {
        if (!canMoveTo(next)) {
            error("global cross dock variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global cross dock variance"} $code"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): GlobalCrossDockVariance {
        return GlobalCrossDockVariance(
            code = code,
            title = title,
            active = active,
            expiresAt = expiresAt,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global cross dock variance in status planned. */
fun newGlobalCrossDockVariance(code: String, title: String): GlobalCrossDockVariance {
    return GlobalCrossDockVariance(
        code = code,
        title = title,
        active = false,
        expiresAt = 0,
        depot = "",
        status = GlobalCrossDockVarianceStatus.PLANNED,
    )
}
