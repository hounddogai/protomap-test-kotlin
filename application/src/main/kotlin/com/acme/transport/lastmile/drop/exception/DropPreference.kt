package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop preference is in its life cycle. */
enum class DropPreferenceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a drop preference and its current status. */
data class DropPreference(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var amount: Double,
    var durationMinutes: Int,
    var description: String,
    var phone: String,
    var dock: String,
    var capacity: Int,
    var endsAt: Int,
    var status: DropPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropPreferenceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropPreferenceStatus): Boolean {
        when (status) {
            DropPreferenceStatus.DRAFT -> return next == DropPreferenceStatus.PENDING || next == DropPreferenceStatus.SUSPENDED
            DropPreferenceStatus.PENDING -> return next == DropPreferenceStatus.SUSPENDED
            DropPreferenceStatus.ACTIVE -> return next == DropPreferenceStatus.SUSPENDED
            DropPreferenceStatus.SUSPENDED -> {
                when (next) {
                    DropPreferenceStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("drop preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"drop preference"} $code"

    fun adjustedExposure(): Double = round((durationMinutes.toDouble() - amount) * 100) / 100

    fun snapshot(): DropPreference {
        return DropPreference(
            code = code,
            label = label,
            expiresAt = expiresAt,
            amount = amount,
            durationMinutes = durationMinutes,
            description = description,
            phone = phone,
            dock = dock,
            capacity = capacity,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a drop preference in status draft. */
fun newDropPreference(code: String, label: String): DropPreference {
    return DropPreference(
        code = code,
        label = label,
        expiresAt = 0,
        amount = 0.0,
        durationMinutes = 0,
        description = "",
        phone = "",
        dock = "",
        capacity = 0,
        endsAt = 0,
        status = DropPreferenceStatus.DRAFT,
    )
}
