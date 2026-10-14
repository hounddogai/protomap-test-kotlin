package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling duty period draft is in its life cycle. */
enum class RollingDutyPeriodDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling duty period draft. Its code is unique within a tenant. */
data class RollingDutyPeriodDraft(
    var code: String,
    var label: String,
    var attempts: Int,
    var version: Int,
    var phone: String,
    var summary: String,
    var amount: Double,
    var attributes: MutableMap<String, String>,
    var ownerId: String,
    var status: RollingDutyPeriodDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDutyPeriodDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDutyPeriodDraftStatus): Boolean {
        when (status) {
            RollingDutyPeriodDraftStatus.NEW -> return next == RollingDutyPeriodDraftStatus.QUEUED
            RollingDutyPeriodDraftStatus.QUEUED -> {
                when (next) {
                    RollingDutyPeriodDraftStatus.DONE, RollingDutyPeriodDraftStatus.FAILED -> return true
                }
                return false
            }
            RollingDutyPeriodDraftStatus.RUNNING -> {
                when (next) {
                    RollingDutyPeriodDraftStatus.DONE, RollingDutyPeriodDraftStatus.FAILED -> return true
                }
                return false
            }
            RollingDutyPeriodDraftStatus.DONE -> return next == RollingDutyPeriodDraftStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingDutyPeriodDraftStatus) {
        if (!canMoveTo(next)) {
            error("rolling duty period draft $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"rolling duty period draft"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((attempts.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): RollingDutyPeriodDraft {
        return RollingDutyPeriodDraft(
            code = code,
            label = label,
            attempts = attempts,
            version = version,
            phone = phone,
            summary = summary,
            amount = amount,
            attributes = attributes,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling duty period draft in status new. */
fun newRollingDutyPeriodDraft(code: String, label: String): RollingDutyPeriodDraft {
    return RollingDutyPeriodDraft(
        code = code,
        label = label,
        attempts = 0,
        version = 0,
        phone = "",
        summary = "",
        amount = 0.0,
        attributes = mutableMapOf<String, String>(),
        ownerId = "",
        status = RollingDutyPeriodDraftStatus.NEW,
    )
}
