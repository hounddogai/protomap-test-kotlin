package com.acme.transport.network.zone.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy zone tier is in its life cycle. */
enum class LegacyZoneTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy zone tier and its current status. */
data class LegacyZoneTier(
    var code: String,
    var name: String,
    var archived: Boolean,
    var weight: Double,
    var retries: Int,
    var endsAt: Int,
    var ratio: Double,
    var unit: String,
    var depot: String,
    var attempts: Int,
    var status: LegacyZoneTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyZoneTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyZoneTierStatus): Boolean {
        when (status) {
            LegacyZoneTierStatus.NEW -> {
                when (next) {
                    LegacyZoneTierStatus.DONE -> return true
                }
                return false
            }
            LegacyZoneTierStatus.QUEUED -> {
                when (next) {
                    LegacyZoneTierStatus.RUNNING, LegacyZoneTierStatus.FAILED -> return true
                }
                return false
            }
            LegacyZoneTierStatus.RUNNING -> {
                when (next) {
                    LegacyZoneTierStatus.DONE, LegacyZoneTierStatus.FAILED -> return true
                }
                return false
            }
            LegacyZoneTierStatus.DONE -> {
                when (next) {
                    LegacyZoneTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyZoneTierStatus) {
        if (!canMoveTo(next)) {
            error("legacy zone tier $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round(endsAt.toDouble() * ratio * 100) / 100

    fun snapshot(): LegacyZoneTier {
        return LegacyZoneTier(
            code = code,
            name = name,
            archived = archived,
            weight = weight,
            retries = retries,
            endsAt = endsAt,
            ratio = ratio,
            unit = unit,
            depot = depot,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a legacy zone tier in status new. */
fun newLegacyZoneTier(code: String, name: String): LegacyZoneTier {
    return LegacyZoneTier(
        code = code,
        name = name,
        archived = false,
        weight = 0.0,
        retries = 0,
        endsAt = 0,
        ratio = 0.0,
        unit = "",
        depot = "",
        attempts = 0,
        status = LegacyZoneTierStatus.NEW,
    )
}
