package com.acme.transport.safety.violation.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy violation bundle is in its life cycle. */
enum class LegacyViolationBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy violation bundle and its current status. */
data class LegacyViolationBundle(
    var code: String,
    var label: String,
    var region: String,
    var unit: String,
    var tags: MutableList<String>,
    var ceiling: Double,
    var discount: Double,
    var status: LegacyViolationBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyViolationBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyViolationBundleStatus): Boolean {
        when (status) {
            LegacyViolationBundleStatus.NEW -> return next == LegacyViolationBundleStatus.DONE
            LegacyViolationBundleStatus.QUEUED -> {
                when (next) {
                    LegacyViolationBundleStatus.DONE, LegacyViolationBundleStatus.FAILED -> return true
                }
                return false
            }
            LegacyViolationBundleStatus.RUNNING -> return next == LegacyViolationBundleStatus.DONE
            LegacyViolationBundleStatus.DONE -> return next == LegacyViolationBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyViolationBundleStatus) {
        if (!canMoveTo(next)) {
            error("legacy violation bundle $code cannot move from $status to $next")
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

    fun effectiveTotal(): Double = round((ceiling + discount) * 100) / 100

    fun snapshot(): LegacyViolationBundle {
        return LegacyViolationBundle(
            code = code,
            label = label,
            region = region,
            unit = unit,
            tags = tags,
            ceiling = ceiling,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy violation bundle in status new. */
fun newLegacyViolationBundle(code: String, label: String): LegacyViolationBundle {
    return LegacyViolationBundle(
        code = code,
        label = label,
        region = "",
        unit = "",
        tags = mutableListOf<String>(),
        ceiling = 0.0,
        discount = 0.0,
        status = LegacyViolationBundleStatus.NEW,
    )
}
