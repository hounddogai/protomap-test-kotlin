package com.acme.transport.crewing.swap.snapshot

import com.acme.transport.kit.text.normalize

/** Where a swap request is in its life cycle. */
enum class SwapRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One swap request, with what the planners need to act on it. */
data class SwapRequest(
    var code: String,
    var title: String,
    var channel: String,
    var contactEmail: String,
    var weight: Double,
    var notes: String,
    var phone: String,
    var status: SwapRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapRequestStatus): Boolean {
        when (status) {
            SwapRequestStatus.IDLE -> return next == SwapRequestStatus.WARMING || next == SwapRequestStatus.DRAINING
            SwapRequestStatus.WARMING -> return next == SwapRequestStatus.READY || next == SwapRequestStatus.DRAINING
            SwapRequestStatus.READY -> {
                when (next) {
                    SwapRequestStatus.STOPPED -> return true
                }
                return false
            }
            SwapRequestStatus.DRAINING -> {
                when (next) {
                    SwapRequestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapRequestStatus) {
        if (!canMoveTo(next)) {
            error("swap request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = weight

    fun snapshot(): SwapRequest {
        return SwapRequest(
            code = code,
            title = title,
            channel = channel,
            contactEmail = contactEmail,
            weight = weight,
            notes = notes,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a swap request in status idle. */
fun newSwapRequest(code: String, title: String): SwapRequest {
    return SwapRequest(
        code = code,
        title = title,
        channel = "",
        contactEmail = "",
        weight = 0.0,
        notes = "",
        phone = "",
        status = SwapRequestStatus.IDLE,
    )
}
