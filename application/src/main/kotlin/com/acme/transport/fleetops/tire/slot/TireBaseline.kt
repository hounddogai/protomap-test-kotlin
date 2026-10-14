package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.text.normalize

/** Where a tire baseline is in its life cycle. */
enum class TireBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tire baseline and its current status. */
data class TireBaseline(
    var code: String,
    var title: String,
    var depot: String,
    var trailerId: String,
    var pallets: Int,
    var country: String,
    var status: TireBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireBaselineStatus): Boolean {
        when (status) {
            TireBaselineStatus.NEW -> {
                when (next) {
                    TireBaselineStatus.FAILED -> return true
                }
                return false
            }
            TireBaselineStatus.QUEUED -> {
                when (next) {
                    TireBaselineStatus.RUNNING -> return true
                }
                return false
            }
            TireBaselineStatus.RUNNING -> {
                when (next) {
                    TireBaselineStatus.FAILED -> return true
                }
                return false
            }
            TireBaselineStatus.DONE -> {
                when (next) {
                    TireBaselineStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireBaselineStatus) {
        if (!canMoveTo(next)) {
            error("tire baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tire baseline"} $code"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): TireBaseline {
        return TireBaseline(
            code = code,
            title = title,
            depot = depot,
            trailerId = trailerId,
            pallets = pallets,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tire baseline in status new. */
fun newTireBaseline(code: String, title: String): TireBaseline {
    return TireBaseline(
        code = code,
        title = title,
        depot = "",
        trailerId = "",
        pallets = 0,
        country = "",
        status = TireBaselineStatus.NEW,
    )
}
