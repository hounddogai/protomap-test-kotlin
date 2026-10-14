package com.acme.transport.terminal.dwell.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback dwell preference is in its life cycle. */
enum class FallbackDwellPreferenceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback dwell preference. Its code is unique within a tenant. */
data class FallbackDwellPreference(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var discount: Double,
    var pinned: Boolean,
    var weight: Double,
    var status: FallbackDwellPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDwellPreferenceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDwellPreferenceStatus): Boolean {
        when (status) {
            FallbackDwellPreferenceStatus.DRAFT -> {
                when (next) {
                    FallbackDwellPreferenceStatus.CLOSED -> return true
                }
                return false
            }
            FallbackDwellPreferenceStatus.PENDING -> {
                when (next) {
                    FallbackDwellPreferenceStatus.ACTIVE, FallbackDwellPreferenceStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackDwellPreferenceStatus.ACTIVE -> {
                when (next) {
                    FallbackDwellPreferenceStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackDwellPreferenceStatus.SUSPENDED -> {
                when (next) {
                    FallbackDwellPreferenceStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDwellPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("fallback dwell preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round(expiresAt.toDouble() * discount * 100) / 100

    fun snapshot(): FallbackDwellPreference {
        return FallbackDwellPreference(
            code = code,
            name = name,
            expiresAt = expiresAt,
            discount = discount,
            pinned = pinned,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback dwell preference in status draft. */
fun newFallbackDwellPreference(code: String, name: String): FallbackDwellPreference {
    return FallbackDwellPreference(
        code = code,
        name = name,
        expiresAt = 0,
        discount = 0.0,
        pinned = false,
        weight = 0.0,
        status = FallbackDwellPreferenceStatus.DRAFT,
    )
}
