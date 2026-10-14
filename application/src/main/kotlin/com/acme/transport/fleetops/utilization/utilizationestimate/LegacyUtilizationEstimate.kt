package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy utilization estimate is in its life cycle. */
enum class LegacyUtilizationEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy utilization estimate as the utilization team keeps it. */
data class LegacyUtilizationEstimate(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var endsAt: Int,
    var discount: Double,
    var status: LegacyUtilizationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyUtilizationEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyUtilizationEstimateStatus): Boolean {
        when (status) {
            LegacyUtilizationEstimateStatus.NEW -> {
                when (next) {
                    LegacyUtilizationEstimateStatus.RUNNING, LegacyUtilizationEstimateStatus.FAILED -> return true
                }
                return false
            }
            LegacyUtilizationEstimateStatus.QUEUED -> return next == LegacyUtilizationEstimateStatus.RUNNING || next == LegacyUtilizationEstimateStatus.DONE
            LegacyUtilizationEstimateStatus.RUNNING -> return next == LegacyUtilizationEstimateStatus.FAILED
            LegacyUtilizationEstimateStatus.DONE -> {
                when (next) {
                    LegacyUtilizationEstimateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyUtilizationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("legacy utilization estimate $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round(endsAt.toDouble() * discount * 100) / 100

    fun snapshot(): LegacyUtilizationEstimate {
        return LegacyUtilizationEstimate(
            code = code,
            title = title,
            attributes = attributes,
            endsAt = endsAt,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy utilization estimate in status new. */
fun newLegacyUtilizationEstimate(code: String, title: String): LegacyUtilizationEstimate {
    return LegacyUtilizationEstimate(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        endsAt = 0,
        discount = 0.0,
        status = LegacyUtilizationEstimateStatus.NEW,
    )
}
