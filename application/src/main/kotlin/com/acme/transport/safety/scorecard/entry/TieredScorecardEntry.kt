package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered scorecard entry records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a tiered scorecard entry is in its life cycle. */
enum class TieredScorecardEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tiered scorecard entry, with what the planners need to act on it. */
data class TieredScorecardEntry(
    var code: String,
    var name: String,
    var active: Boolean,
    var revision: Int,
    var externalRef: String,
    var retries: Int,
    var rate: Double,
    var status: TieredScorecardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredScorecardEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredScorecardEntryStatus): Boolean {
        when (status) {
            TieredScorecardEntryStatus.CREATED -> {
                when (next) {
                    TieredScorecardEntryStatus.VALIDATED, TieredScorecardEntryStatus.PUBLISHED -> return true
                }
                return false
            }
            TieredScorecardEntryStatus.VALIDATED -> {
                when (next) {
                    TieredScorecardEntryStatus.RETIRED -> return true
                }
                return false
            }
            TieredScorecardEntryStatus.PUBLISHED -> {
                when (next) {
                    TieredScorecardEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredScorecardEntryStatus) {
        if (!canMoveTo(next)) {
            error("tiered scorecard entry $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round((rate + retries.toDouble()) * 100) / 100

    fun snapshot(): TieredScorecardEntry {
        return TieredScorecardEntry(
            code = code,
            name = name,
            active = active,
            revision = revision,
            externalRef = externalRef,
            retries = retries,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered scorecard entry in status created. */
fun newTieredScorecardEntry(code: String, name: String): TieredScorecardEntry {
    return TieredScorecardEntry(
        code = code,
        name = name,
        active = false,
        revision = 0,
        externalRef = "",
        retries = 0,
        rate = 0.0,
        status = TieredScorecardEntryStatus.CREATED,
    )
}
