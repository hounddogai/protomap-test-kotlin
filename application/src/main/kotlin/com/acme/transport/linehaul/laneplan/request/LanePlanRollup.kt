package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan rollup is in its life cycle. */
enum class LanePlanRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lane plan rollup as the lane plan team keeps it. */
data class LanePlanRollup(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var weight: Double,
    var maximum: Double,
    var status: LanePlanRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanRollupStatus): Boolean {
        when (status) {
            LanePlanRollupStatus.REQUESTED -> {
                when (next) {
                    LanePlanRollupStatus.REVOKED -> return true
                }
                return false
            }
            LanePlanRollupStatus.GRANTED -> {
                when (next) {
                    LanePlanRollupStatus.EXPIRED -> return true
                }
                return false
            }
            LanePlanRollupStatus.REVOKED -> return next == LanePlanRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LanePlanRollupStatus) {
        if (!canMoveTo(next)) {
            error("lane plan rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((maximum - weight) * 100) / 100

    fun snapshot(): LanePlanRollup {
        return LanePlanRollup(
            code = code,
            title = title,
            attributes = attributes,
            weight = weight,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lane plan rollup in status requested. */
fun newLanePlanRollup(code: String, title: String): LanePlanRollup {
    return LanePlanRollup(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        weight = 0.0,
        maximum = 0.0,
        status = LanePlanRollupStatus.REQUESTED,
    )
}
