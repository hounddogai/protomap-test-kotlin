package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered rest break preference records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a tiered rest break preference is in its life cycle. */
enum class TieredRestBreakPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One tiered rest break preference, with what the planners need to act on it. */
data class TieredRestBreakPreference(
    var code: String,
    var label: String,
    var rate: Double,
    var trailerId: String,
    var locale: String,
    var etaMinutes: Int,
    var quantity: Int,
    var createdAt: Int,
    var active: Boolean,
    var category: String,
    var status: TieredRestBreakPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRestBreakPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRestBreakPreferenceStatus): Boolean {
        when (status) {
            TieredRestBreakPreferenceStatus.REQUESTED -> return next == TieredRestBreakPreferenceStatus.REVOKED || next == TieredRestBreakPreferenceStatus.EXPIRED
            TieredRestBreakPreferenceStatus.GRANTED -> {
                when (next) {
                    TieredRestBreakPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            TieredRestBreakPreferenceStatus.REVOKED -> {
                when (next) {
                    TieredRestBreakPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredRestBreakPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("tiered rest break preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netTotal(): Double = round((rate - createdAt.toDouble()) * 100) / 100

    fun snapshot(): TieredRestBreakPreference {
        return TieredRestBreakPreference(
            code = code,
            label = label,
            rate = rate,
            trailerId = trailerId,
            locale = locale,
            etaMinutes = etaMinutes,
            quantity = quantity,
            createdAt = createdAt,
            active = active,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered rest break preference in status requested. */
fun newTieredRestBreakPreference(code: String, label: String): TieredRestBreakPreference {
    return TieredRestBreakPreference(
        code = code,
        label = label,
        rate = 0.0,
        trailerId = "",
        locale = "",
        etaMinutes = 0,
        quantity = 0,
        createdAt = 0,
        active = false,
        category = "",
        status = TieredRestBreakPreferenceStatus.REQUESTED,
    )
}
