package com.acme.transport.crewing.swap.draft

import com.acme.transport.kit.text.normalize

/** Where a swap metric is in its life cycle. */
enum class SwapMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A swap metric. Its code is unique within a tenant. */
data class SwapMetric(
    var code: String,
    var title: String,
    var shiftCode: String,
    var source: String,
    var quantity: Int,
    var region: String,
    var category: String,
    var status: SwapMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapMetricStatus): Boolean {
        when (status) {
            SwapMetricStatus.PROPOSED -> return next == SwapMetricStatus.WITHDRAWN
            SwapMetricStatus.APPROVED -> return next == SwapMetricStatus.REJECTED
            SwapMetricStatus.REJECTED -> {
                when (next) {
                    SwapMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapMetricStatus) {
        if (!canMoveTo(next)) {
            error("swap metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): SwapMetric {
        return SwapMetric(
            code = code,
            title = title,
            shiftCode = shiftCode,
            source = source,
            quantity = quantity,
            region = region,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a swap metric in status proposed. */
fun newSwapMetric(code: String, title: String): SwapMetric {
    return SwapMetric(
        code = code,
        title = title,
        shiftCode = "",
        source = "",
        quantity = 0,
        region = "",
        category = "",
        status = SwapMetricStatus.PROPOSED,
    )
}
