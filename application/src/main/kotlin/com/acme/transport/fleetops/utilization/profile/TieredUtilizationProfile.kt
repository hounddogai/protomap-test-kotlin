package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered utilization profile records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a tiered utilization profile is in its life cycle. */
enum class TieredUtilizationProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tiered utilization profile as the utilization team keeps it. */
data class TieredUtilizationProfile(
    var code: String,
    var name: String,
    var rate: Double,
    var attributes: MutableMap<String, String>,
    var expiresAt: Int,
    var sequence: Int,
    var revision: Int,
    var odometer: Int,
    var ratio: Double,
    var limit: Int,
    var status: TieredUtilizationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredUtilizationProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredUtilizationProfileStatus): Boolean {
        when (status) {
            TieredUtilizationProfileStatus.PLANNED -> {
                when (next) {
                    TieredUtilizationProfileStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TieredUtilizationProfileStatus.SCHEDULED -> {
                when (next) {
                    TieredUtilizationProfileStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TieredUtilizationProfileStatus.IN_PROGRESS -> {
                when (next) {
                    TieredUtilizationProfileStatus.COMPLETED, TieredUtilizationProfileStatus.CANCELLED -> return true
                }
                return false
            }
            TieredUtilizationProfileStatus.COMPLETED -> return next == TieredUtilizationProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredUtilizationProfileStatus) {
        if (!canMoveTo(next)) {
            error("tiered utilization profile $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"tiered utilization profile"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((revision.toDouble() - ratio) * 100) / 100

    fun snapshot(): TieredUtilizationProfile {
        return TieredUtilizationProfile(
            code = code,
            name = name,
            rate = rate,
            attributes = attributes,
            expiresAt = expiresAt,
            sequence = sequence,
            revision = revision,
            odometer = odometer,
            ratio = ratio,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered utilization profile in status planned. */
fun newTieredUtilizationProfile(code: String, name: String): TieredUtilizationProfile {
    return TieredUtilizationProfile(
        code = code,
        name = name,
        rate = 0.0,
        attributes = mutableMapOf<String, String>(),
        expiresAt = 0,
        sequence = 0,
        revision = 0,
        odometer = 0,
        ratio = 0.0,
        limit = 0,
        status = TieredUtilizationProfileStatus.PLANNED,
    )
}
