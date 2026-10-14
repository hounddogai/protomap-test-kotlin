package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled cutoff preference is in its life cycle. */
enum class ScheduledCutoffPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled cutoff preference. Its code is unique within a tenant. */
data class ScheduledCutoffPreference(
    var code: String,
    var title: String,
    var locale: String,
    var sequence: Int,
    var description: String,
    var updatedAt: Int,
    var discount: Double,
    var category: String,
    var createdAt: Int,
    var status: ScheduledCutoffPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCutoffPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCutoffPreferenceStatus): Boolean {
        when (status) {
            ScheduledCutoffPreferenceStatus.REQUESTED -> return next == ScheduledCutoffPreferenceStatus.GRANTED || next == ScheduledCutoffPreferenceStatus.REVOKED
            ScheduledCutoffPreferenceStatus.GRANTED -> return next == ScheduledCutoffPreferenceStatus.REVOKED || next == ScheduledCutoffPreferenceStatus.EXPIRED
            ScheduledCutoffPreferenceStatus.REVOKED -> return next == ScheduledCutoffPreferenceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScheduledCutoffPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("scheduled cutoff preference $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled cutoff preference"} $code"

    fun weightedLoad(): Double = round(updatedAt.toDouble() * discount * 100) / 100

    fun snapshot(): ScheduledCutoffPreference {
        return ScheduledCutoffPreference(
            code = code,
            title = title,
            locale = locale,
            sequence = sequence,
            description = description,
            updatedAt = updatedAt,
            discount = discount,
            category = category,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled cutoff preference in status requested. */
fun newScheduledCutoffPreference(code: String, title: String): ScheduledCutoffPreference {
    return ScheduledCutoffPreference(
        code = code,
        title = title,
        locale = "",
        sequence = 0,
        description = "",
        updatedAt = 0,
        discount = 0.0,
        category = "",
        createdAt = 0,
        status = ScheduledCutoffPreferenceStatus.REQUESTED,
    )
}
