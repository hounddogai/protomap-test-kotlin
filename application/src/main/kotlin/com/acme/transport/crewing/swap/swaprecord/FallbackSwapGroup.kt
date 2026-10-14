package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.kit.text.normalize

/** Where a fallback swap group is in its life cycle. */
enum class FallbackSwapGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a fallback swap group and its current status. */
data class FallbackSwapGroup(
    var code: String,
    var label: String,
    var contactEmail: String,
    var title: String,
    var endsAt: Int,
    var status: FallbackSwapGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackSwapGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackSwapGroupStatus): Boolean {
        when (status) {
            FallbackSwapGroupStatus.REQUESTED -> return next == FallbackSwapGroupStatus.GRANTED
            FallbackSwapGroupStatus.GRANTED -> {
                when (next) {
                    FallbackSwapGroupStatus.REVOKED, FallbackSwapGroupStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackSwapGroupStatus.REVOKED -> {
                when (next) {
                    FallbackSwapGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackSwapGroupStatus) {
        if (!canMoveTo(next)) {
            error("fallback swap group $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): FallbackSwapGroup {
        return FallbackSwapGroup(
            code = code,
            label = label,
            contactEmail = contactEmail,
            title = title,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback swap group in status requested. */
fun newFallbackSwapGroup(code: String, label: String): FallbackSwapGroup {
    return FallbackSwapGroup(
        code = code,
        label = label,
        contactEmail = "",
        title = "",
        endsAt = 0,
        status = FallbackSwapGroupStatus.REQUESTED,
    )
}
