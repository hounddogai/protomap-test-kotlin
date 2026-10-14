package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.text.normalize

/** Where a global cross dock summary is in its life cycle. */
enum class GlobalCrossDockSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a global cross dock summary and its current status. */
data class GlobalCrossDockSummary(
    var code: String,
    var title: String,
    var quantity: Int,
    var externalRef: String,
    var shiftCode: String,
    var status: GlobalCrossDockSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCrossDockSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCrossDockSummaryStatus): Boolean {
        when (status) {
            GlobalCrossDockSummaryStatus.IDLE -> return next == GlobalCrossDockSummaryStatus.WARMING || next == GlobalCrossDockSummaryStatus.READY
            GlobalCrossDockSummaryStatus.WARMING -> {
                when (next) {
                    GlobalCrossDockSummaryStatus.DRAINING, GlobalCrossDockSummaryStatus.STOPPED -> return true
                }
                return false
            }
            GlobalCrossDockSummaryStatus.READY -> return next == GlobalCrossDockSummaryStatus.DRAINING || next == GlobalCrossDockSummaryStatus.STOPPED
            GlobalCrossDockSummaryStatus.DRAINING -> return next == GlobalCrossDockSummaryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalCrossDockSummaryStatus) {
        if (!canMoveTo(next)) {
            error("global cross dock summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): GlobalCrossDockSummary {
        return GlobalCrossDockSummary(
            code = code,
            title = title,
            quantity = quantity,
            externalRef = externalRef,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global cross dock summary in status idle. */
fun newGlobalCrossDockSummary(code: String, title: String): GlobalCrossDockSummary {
    return GlobalCrossDockSummary(
        code = code,
        title = title,
        quantity = 0,
        externalRef = "",
        shiftCode = "",
        status = GlobalCrossDockSummaryStatus.IDLE,
    )
}
