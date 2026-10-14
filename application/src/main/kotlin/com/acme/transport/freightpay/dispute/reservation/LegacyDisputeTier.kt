package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy dispute tier is in its life cycle. */
enum class LegacyDisputeTierStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a legacy dispute tier and its current status. */
data class LegacyDisputeTier(
    var code: String,
    var title: String,
    var createdAt: Int,
    var ratio: Double,
    var startsAt: Int,
    var country: String,
    var floorValue: Double,
    var loadKg: Double,
    var score: Double,
    var durationMinutes: Int,
    var status: LegacyDisputeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDisputeTierStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDisputeTierStatus): Boolean {
        when (status) {
            LegacyDisputeTierStatus.DRAFT -> return next == LegacyDisputeTierStatus.ACTIVE
            LegacyDisputeTierStatus.PENDING -> return next == LegacyDisputeTierStatus.SUSPENDED || next == LegacyDisputeTierStatus.CLOSED
            LegacyDisputeTierStatus.ACTIVE -> {
                when (next) {
                    LegacyDisputeTierStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyDisputeTierStatus.SUSPENDED -> {
                when (next) {
                    LegacyDisputeTierStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyDisputeTierStatus) {
        if (!canMoveTo(next)) {
            error("legacy dispute tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy dispute tier"} $code"

    fun projectedTotal(): Double = round(loadKg * startsAt.toDouble() * 100) / 100

    fun snapshot(): LegacyDisputeTier {
        return LegacyDisputeTier(
            code = code,
            title = title,
            createdAt = createdAt,
            ratio = ratio,
            startsAt = startsAt,
            country = country,
            floorValue = floorValue,
            loadKg = loadKg,
            score = score,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy dispute tier in status draft. */
fun newLegacyDisputeTier(code: String, title: String): LegacyDisputeTier {
    return LegacyDisputeTier(
        code = code,
        title = title,
        createdAt = 0,
        ratio = 0.0,
        startsAt = 0,
        country = "",
        floorValue = 0.0,
        loadKg = 0.0,
        score = 0.0,
        durationMinutes = 0,
        status = LegacyDisputeTierStatus.DRAFT,
    )
}
