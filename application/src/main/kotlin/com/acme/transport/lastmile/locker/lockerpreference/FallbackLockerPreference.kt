package com.acme.transport.lastmile.locker.lockerpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback locker preference records of lastmile's locker work, and the rules, stores, and calculations
// around them.

/** Where a fallback locker preference is in its life cycle. */
enum class FallbackLockerPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback locker preference as the locker team keeps it. */
data class FallbackLockerPreference(
    var code: String,
    var label: String,
    var active: Boolean,
    var attempts: Int,
    var createdAt: Int,
    var visible: Boolean,
    var sequence: Int,
    var windowDays: Int,
    var floorValue: Double,
    var revision: Int,
    var discount: Double,
    var status: FallbackLockerPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLockerPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLockerPreferenceStatus): Boolean {
        when (status) {
            FallbackLockerPreferenceStatus.IDLE -> {
                when (next) {
                    FallbackLockerPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            FallbackLockerPreferenceStatus.WARMING -> {
                when (next) {
                    FallbackLockerPreferenceStatus.READY -> return true
                }
                return false
            }
            FallbackLockerPreferenceStatus.READY -> return next == FallbackLockerPreferenceStatus.DRAINING || next == FallbackLockerPreferenceStatus.STOPPED
            FallbackLockerPreferenceStatus.DRAINING -> return next == FallbackLockerPreferenceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackLockerPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("fallback locker preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round((attempts.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): FallbackLockerPreference {
        return FallbackLockerPreference(
            code = code,
            label = label,
            active = active,
            attempts = attempts,
            createdAt = createdAt,
            visible = visible,
            sequence = sequence,
            windowDays = windowDays,
            floorValue = floorValue,
            revision = revision,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback locker preference in status idle. */
fun newFallbackLockerPreference(code: String, label: String): FallbackLockerPreference {
    return FallbackLockerPreference(
        code = code,
        label = label,
        active = false,
        attempts = 0,
        createdAt = 0,
        visible = false,
        sequence = 0,
        windowDays = 0,
        floorValue = 0.0,
        revision = 0,
        discount = 0.0,
        status = FallbackLockerPreferenceStatus.IDLE,
    )
}
