package com.acme.transport.crewing.swap.schedule

import com.acme.transport.kit.text.normalize

/** Where a swap quota is in its life cycle. */
enum class SwapQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One swap quota, with what the planners need to act on it. */
data class SwapQuota(
    var code: String,
    var title: String,
    var amount: Double,
    var locale: String,
    var active: Boolean,
    var status: SwapQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapQuotaStatus): Boolean {
        when (status) {
            SwapQuotaStatus.NEW -> return next == SwapQuotaStatus.DONE
            SwapQuotaStatus.QUEUED -> return next == SwapQuotaStatus.RUNNING || next == SwapQuotaStatus.FAILED
            SwapQuotaStatus.RUNNING -> return next == SwapQuotaStatus.FAILED
            SwapQuotaStatus.DONE -> {
                when (next) {
                    SwapQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapQuotaStatus) {
        if (!canMoveTo(next)) {
            error("swap quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = amount

    fun snapshot(): SwapQuota {
        return SwapQuota(
            code = code,
            title = title,
            amount = amount,
            locale = locale,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a swap quota in status new. */
fun newSwapQuota(code: String, title: String): SwapQuota {
    return SwapQuota(
        code = code,
        title = title,
        amount = 0.0,
        locale = "",
        active = false,
        status = SwapQuotaStatus.NEW,
    )
}
