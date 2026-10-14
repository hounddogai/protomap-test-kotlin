package com.acme.transport.safety.violation.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary violation preference records of safety's violation work, and the rules, stores, and
// calculations around them.

/** Where a primary violation preference is in its life cycle. */
enum class PrimaryViolationPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary violation preference, with what the planners need to act on it. */
data class PrimaryViolationPreference(
    var code: String,
    var title: String,
    var unit: String,
    var etaMinutes: Int,
    var priority: Int,
    var currency: String,
    var category: String,
    var status: PrimaryViolationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryViolationPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryViolationPreferenceStatus): Boolean {
        when (status) {
            PrimaryViolationPreferenceStatus.NEW -> {
                when (next) {
                    PrimaryViolationPreferenceStatus.DONE, PrimaryViolationPreferenceStatus.FAILED -> return true
                }
                return false
            }
            PrimaryViolationPreferenceStatus.QUEUED -> {
                when (next) {
                    PrimaryViolationPreferenceStatus.RUNNING -> return true
                }
                return false
            }
            PrimaryViolationPreferenceStatus.RUNNING -> return next == PrimaryViolationPreferenceStatus.FAILED
            PrimaryViolationPreferenceStatus.DONE -> return next == PrimaryViolationPreferenceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryViolationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("primary violation preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round(etaMinutes.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): PrimaryViolationPreference {
        return PrimaryViolationPreference(
            code = code,
            title = title,
            unit = unit,
            etaMinutes = etaMinutes,
            priority = priority,
            currency = currency,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary violation preference in status new. */
fun newPrimaryViolationPreference(code: String, title: String): PrimaryViolationPreference {
    return PrimaryViolationPreference(
        code = code,
        title = title,
        unit = "",
        etaMinutes = 0,
        priority = 0,
        currency = "",
        category = "",
        status = PrimaryViolationPreferenceStatus.NEW,
    )
}
