package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy redelivery variance is in its life cycle. */
enum class LegacyRedeliveryVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a legacy redelivery variance and its current status. */
data class LegacyRedeliveryVariance(
    var code: String,
    var name: String,
    var revision: Int,
    var updatedAt: Int,
    var cost: Double,
    var weight: Double,
    var notes: String,
    var pallets: Int,
    var timezone: String,
    var checksum: String,
    var status: LegacyRedeliveryVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRedeliveryVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRedeliveryVarianceStatus): Boolean {
        when (status) {
            LegacyRedeliveryVarianceStatus.IDLE -> {
                when (next) {
                    LegacyRedeliveryVarianceStatus.DRAINING, LegacyRedeliveryVarianceStatus.STOPPED -> return true
                }
                return false
            }
            LegacyRedeliveryVarianceStatus.WARMING -> {
                when (next) {
                    LegacyRedeliveryVarianceStatus.READY, LegacyRedeliveryVarianceStatus.STOPPED -> return true
                }
                return false
            }
            LegacyRedeliveryVarianceStatus.READY -> {
                when (next) {
                    LegacyRedeliveryVarianceStatus.STOPPED -> return true
                }
                return false
            }
            LegacyRedeliveryVarianceStatus.DRAINING -> return next == LegacyRedeliveryVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyRedeliveryVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy redelivery variance $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"legacy redelivery variance"} $code"

    fun effectiveVolume(): Double = round((pallets.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyRedeliveryVariance {
        return LegacyRedeliveryVariance(
            code = code,
            name = name,
            revision = revision,
            updatedAt = updatedAt,
            cost = cost,
            weight = weight,
            notes = notes,
            pallets = pallets,
            timezone = timezone,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy redelivery variance in status idle. */
fun newLegacyRedeliveryVariance(code: String, name: String): LegacyRedeliveryVariance {
    return LegacyRedeliveryVariance(
        code = code,
        name = name,
        revision = 0,
        updatedAt = 0,
        cost = 0.0,
        weight = 0.0,
        notes = "",
        pallets = 0,
        timezone = "",
        checksum = "",
        status = LegacyRedeliveryVarianceStatus.IDLE,
    )
}
