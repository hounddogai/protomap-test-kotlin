package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scorecard bundle records of safety's scorecard work, and the rules, stores, and calculations around
// them.

/** Where a scorecard bundle is in its life cycle. */
enum class ScorecardBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scorecard bundle. Its code is unique within a tenant. */
data class ScorecardBundle(
    var code: String,
    var label: String,
    var minimum: Double,
    var fuelLiters: Double,
    var threshold: Double,
    var visible: Boolean,
    var amount: Double,
    var status: ScorecardBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardBundleStatus): Boolean {
        when (status) {
            ScorecardBundleStatus.OPEN -> {
                when (next) {
                    ScorecardBundleStatus.IN_REVIEW, ScorecardBundleStatus.RESOLVED -> return true
                }
                return false
            }
            ScorecardBundleStatus.IN_REVIEW -> {
                when (next) {
                    ScorecardBundleStatus.REOPENED -> return true
                }
                return false
            }
            ScorecardBundleStatus.RESOLVED -> {
                when (next) {
                    ScorecardBundleStatus.REOPENED, ScorecardBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            ScorecardBundleStatus.REOPENED -> {
                when (next) {
                    ScorecardBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardBundleStatus) {
        if (!canMoveTo(next)) {
            error("scorecard bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round((fuelLiters + threshold) * 100) / 100

    fun snapshot(): ScorecardBundle {
        return ScorecardBundle(
            code = code,
            label = label,
            minimum = minimum,
            fuelLiters = fuelLiters,
            threshold = threshold,
            visible = visible,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scorecard bundle in status open. */
fun newScorecardBundle(code: String, label: String): ScorecardBundle {
    return ScorecardBundle(
        code = code,
        label = label,
        minimum = 0.0,
        fuelLiters = 0.0,
        threshold = 0.0,
        visible = false,
        amount = 0.0,
        status = ScorecardBundleStatus.OPEN,
    )
}
