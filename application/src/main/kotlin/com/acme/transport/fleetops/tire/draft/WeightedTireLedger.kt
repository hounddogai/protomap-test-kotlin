package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted tire ledger is in its life cycle. */
enum class WeightedTireLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted tire ledger as the tire team keeps it. */
data class WeightedTireLedger(
    var code: String,
    var name: String,
    var country: String,
    var category: String,
    var endsAt: Int,
    var durationMinutes: Int,
    var pinned: Boolean,
    var weight: Double,
    var status: WeightedTireLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTireLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTireLedgerStatus): Boolean {
        when (status) {
            WeightedTireLedgerStatus.NEW -> {
                when (next) {
                    WeightedTireLedgerStatus.QUEUED -> return true
                }
                return false
            }
            WeightedTireLedgerStatus.QUEUED -> return next == WeightedTireLedgerStatus.DONE || next == WeightedTireLedgerStatus.FAILED
            WeightedTireLedgerStatus.RUNNING -> {
                when (next) {
                    WeightedTireLedgerStatus.DONE, WeightedTireLedgerStatus.FAILED -> return true
                }
                return false
            }
            WeightedTireLedgerStatus.DONE -> {
                when (next) {
                    WeightedTireLedgerStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedTireLedgerStatus) {
        if (!canMoveTo(next)) {
            error("weighted tire ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"weighted tire ledger"} $code"

    fun effectiveExposure(): Double = round((weight + endsAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedTireLedger {
        return WeightedTireLedger(
            code = code,
            name = name,
            country = country,
            category = category,
            endsAt = endsAt,
            durationMinutes = durationMinutes,
            pinned = pinned,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a weighted tire ledger in status new. */
fun newWeightedTireLedger(code: String, name: String): WeightedTireLedger {
    return WeightedTireLedger(
        code = code,
        name = name,
        country = "",
        category = "",
        endsAt = 0,
        durationMinutes = 0,
        pinned = false,
        weight = 0.0,
        status = WeightedTireLedgerStatus.NEW,
    )
}
