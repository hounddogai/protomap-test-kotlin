package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.text.normalize

/** Where a pending duty period preference is in its life cycle. */
enum class PendingDutyPeriodPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending duty period preference as the duty period team keeps it. */
data class PendingDutyPeriodPreference(
    var code: String,
    var label: String,
    var channel: String,
    var attributes: MutableMap<String, String>,
    var phone: String,
    var revision: Int,
    var dock: String,
    var status: PendingDutyPeriodPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDutyPeriodPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDutyPeriodPreferenceStatus): Boolean {
        when (status) {
            PendingDutyPeriodPreferenceStatus.PLANNED -> {
                when (next) {
                    PendingDutyPeriodPreferenceStatus.SCHEDULED, PendingDutyPeriodPreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            PendingDutyPeriodPreferenceStatus.SCHEDULED -> {
                when (next) {
                    PendingDutyPeriodPreferenceStatus.COMPLETED, PendingDutyPeriodPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            PendingDutyPeriodPreferenceStatus.IN_PROGRESS -> {
                when (next) {
                    PendingDutyPeriodPreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            PendingDutyPeriodPreferenceStatus.COMPLETED -> return next == PendingDutyPeriodPreferenceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingDutyPeriodPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("pending duty period preference $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): PendingDutyPeriodPreference {
        return PendingDutyPeriodPreference(
            code = code,
            label = label,
            channel = channel,
            attributes = attributes,
            phone = phone,
            revision = revision,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending duty period preference in status planned. */
fun newPendingDutyPeriodPreference(code: String, label: String): PendingDutyPeriodPreference {
    return PendingDutyPeriodPreference(
        code = code,
        label = label,
        channel = "",
        attributes = mutableMapOf<String, String>(),
        phone = "",
        revision = 0,
        dock = "",
        status = PendingDutyPeriodPreferenceStatus.PLANNED,
    )
}
