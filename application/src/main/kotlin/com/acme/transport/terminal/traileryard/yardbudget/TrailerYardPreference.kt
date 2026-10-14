package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard preference is in its life cycle. */
enum class TrailerYardPreferenceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One trailer yard preference, with what the planners need to act on it. */
data class TrailerYardPreference(
    var code: String,
    var label: String,
    var batchSize: Int,
    var description: String,
    var graceDays: Int,
    var pallets: Int,
    var expiresAt: Int,
    var windowDays: Int,
    var score: Double,
    var status: TrailerYardPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardPreferenceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardPreferenceStatus): Boolean {
        when (status) {
            TrailerYardPreferenceStatus.DRAFT -> return next == TrailerYardPreferenceStatus.PENDING || next == TrailerYardPreferenceStatus.SUSPENDED
            TrailerYardPreferenceStatus.PENDING -> {
                when (next) {
                    TrailerYardPreferenceStatus.SUSPENDED, TrailerYardPreferenceStatus.CLOSED -> return true
                }
                return false
            }
            TrailerYardPreferenceStatus.ACTIVE -> {
                when (next) {
                    TrailerYardPreferenceStatus.SUSPENDED -> return true
                }
                return false
            }
            TrailerYardPreferenceStatus.SUSPENDED -> return next == TrailerYardPreferenceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TrailerYardPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"trailer yard preference"} $code"

    fun effectiveExposure(): Double = round((batchSize.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardPreference {
        return TrailerYardPreference(
            code = code,
            label = label,
            batchSize = batchSize,
            description = description,
            graceDays = graceDays,
            pallets = pallets,
            expiresAt = expiresAt,
            windowDays = windowDays,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer yard preference in status draft. */
fun newTrailerYardPreference(code: String, label: String): TrailerYardPreference {
    return TrailerYardPreference(
        code = code,
        label = label,
        batchSize = 0,
        description = "",
        graceDays = 0,
        pallets = 0,
        expiresAt = 0,
        windowDays = 0,
        score = 0.0,
        status = TrailerYardPreferenceStatus.DRAFT,
    )
}
