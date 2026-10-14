package com.acme.transport.crewing.swap.proposal

import com.acme.transport.kit.text.normalize

/** Where a swap record is in its life cycle. */
enum class SwapRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One swap record, with what the planners need to act on it. */
data class SwapRecord(
    var code: String,
    var label: String,
    var source: String,
    var reviewer: String,
    var channel: String,
    var quantity: Int,
    var status: SwapRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapRecordStatus): Boolean {
        when (status) {
            SwapRecordStatus.REQUESTED -> return next == SwapRecordStatus.GRANTED
            SwapRecordStatus.GRANTED -> return next == SwapRecordStatus.EXPIRED
            SwapRecordStatus.REVOKED -> {
                when (next) {
                    SwapRecordStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapRecordStatus) {
        if (!canMoveTo(next)) {
            error("swap record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"swap record"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): SwapRecord {
        return SwapRecord(
            code = code,
            label = label,
            source = source,
            reviewer = reviewer,
            channel = channel,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a swap record in status requested. */
fun newSwapRecord(code: String, label: String): SwapRecord {
    return SwapRecord(
        code = code,
        label = label,
        source = "",
        reviewer = "",
        channel = "",
        quantity = 0,
        status = SwapRecordStatus.REQUESTED,
    )
}
