package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global cutoff tier records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a global cutoff tier is in its life cycle. */
enum class GlobalCutoffTierStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global cutoff tier. Its code is unique within a tenant. */
data class GlobalCutoffTier(
    var code: String,
    var name: String,
    var segment: String,
    var category: String,
    var createdAt: Int,
    var phone: String,
    var channel: String,
    var maximum: Double,
    var active: Boolean,
    var trailerId: String,
    var budget: Double,
    var status: GlobalCutoffTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCutoffTierStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCutoffTierStatus): Boolean {
        when (status) {
            GlobalCutoffTierStatus.DRAFT -> {
                when (next) {
                    GlobalCutoffTierStatus.ACTIVE -> return true
                }
                return false
            }
            GlobalCutoffTierStatus.PENDING -> {
                when (next) {
                    GlobalCutoffTierStatus.SUSPENDED -> return true
                }
                return false
            }
            GlobalCutoffTierStatus.ACTIVE -> return next == GlobalCutoffTierStatus.SUSPENDED || next == GlobalCutoffTierStatus.CLOSED
            GlobalCutoffTierStatus.SUSPENDED -> {
                when (next) {
                    GlobalCutoffTierStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCutoffTierStatus) {
        if (!canMoveTo(next)) {
            error("global cutoff tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round((createdAt.toDouble() + budget) * 100) / 100

    fun snapshot(): GlobalCutoffTier {
        return GlobalCutoffTier(
            code = code,
            name = name,
            segment = segment,
            category = category,
            createdAt = createdAt,
            phone = phone,
            channel = channel,
            maximum = maximum,
            active = active,
            trailerId = trailerId,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a global cutoff tier in status draft. */
fun newGlobalCutoffTier(code: String, name: String): GlobalCutoffTier {
    return GlobalCutoffTier(
        code = code,
        name = name,
        segment = "",
        category = "",
        createdAt = 0,
        phone = "",
        channel = "",
        maximum = 0.0,
        active = false,
        trailerId = "",
        budget = 0.0,
        status = GlobalCutoffTierStatus.DRAFT,
    )
}
