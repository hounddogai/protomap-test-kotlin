package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive idling ledger is in its life cycle. */
enum class AdaptiveIdlingLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive idling ledger as the idling team keeps it. */
data class AdaptiveIdlingLedger(
    var code: String,
    var label: String,
    var description: String,
    var unit: String,
    var currency: String,
    var attempts: Int,
    var tags: MutableList<String>,
    var source: String,
    var timezone: String,
    var budget: Double,
    var status: AdaptiveIdlingLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveIdlingLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveIdlingLedgerStatus): Boolean {
        when (status) {
            AdaptiveIdlingLedgerStatus.NEW -> {
                when (next) {
                    AdaptiveIdlingLedgerStatus.DONE -> return true
                }
                return false
            }
            AdaptiveIdlingLedgerStatus.QUEUED -> {
                when (next) {
                    AdaptiveIdlingLedgerStatus.RUNNING, AdaptiveIdlingLedgerStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveIdlingLedgerStatus.RUNNING -> {
                when (next) {
                    AdaptiveIdlingLedgerStatus.DONE -> return true
                }
                return false
            }
            AdaptiveIdlingLedgerStatus.DONE -> {
                when (next) {
                    AdaptiveIdlingLedgerStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveIdlingLedgerStatus) {
        if (!canMoveTo(next)) {
            error("adaptive idling ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round((budget + attempts.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveIdlingLedger {
        return AdaptiveIdlingLedger(
            code = code,
            label = label,
            description = description,
            unit = unit,
            currency = currency,
            attempts = attempts,
            tags = tags,
            source = source,
            timezone = timezone,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive idling ledger in status new. */
fun newAdaptiveIdlingLedger(code: String, label: String): AdaptiveIdlingLedger {
    return AdaptiveIdlingLedger(
        code = code,
        label = label,
        description = "",
        unit = "",
        currency = "",
        attempts = 0,
        tags = mutableListOf<String>(),
        source = "",
        timezone = "",
        budget = 0.0,
        status = AdaptiveIdlingLedgerStatus.NEW,
    )
}
