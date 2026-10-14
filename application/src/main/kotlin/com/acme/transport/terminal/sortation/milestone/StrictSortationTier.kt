package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict sortation tier is in its life cycle. */
enum class StrictSortationTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One strict sortation tier, with what the planners need to act on it. */
data class StrictSortationTier(
    var code: String,
    var title: String,
    var quantity: Int,
    var ratio: Double,
    var retries: Int,
    var active: Boolean,
    var status: StrictSortationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictSortationTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictSortationTierStatus): Boolean {
        when (status) {
            StrictSortationTierStatus.REQUESTED -> {
                when (next) {
                    StrictSortationTierStatus.EXPIRED -> return true
                }
                return false
            }
            StrictSortationTierStatus.GRANTED -> return next == StrictSortationTierStatus.REVOKED
            StrictSortationTierStatus.REVOKED -> {
                when (next) {
                    StrictSortationTierStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictSortationTierStatus) {
        if (!canMoveTo(next)) {
            error("strict sortation tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedValue(): Double = round((ratio + retries.toDouble()) * 100) / 100

    fun snapshot(): StrictSortationTier {
        return StrictSortationTier(
            code = code,
            title = title,
            quantity = quantity,
            ratio = ratio,
            retries = retries,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict sortation tier in status requested. */
fun newStrictSortationTier(code: String, title: String): StrictSortationTier {
    return StrictSortationTier(
        code = code,
        title = title,
        quantity = 0,
        ratio = 0.0,
        retries = 0,
        active = false,
        status = StrictSortationTierStatus.REQUESTED,
    )
}
