package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional lane plan override records of linehaul's lane plan work, and the rules, stores, and
// calculations around them.

/** Where a regional lane plan override is in its life cycle. */
enum class RegionalLanePlanOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a regional lane plan override and its current status. */
data class RegionalLanePlanOverride(
    var code: String,
    var name: String,
    var weight: Double,
    var cost: Double,
    var description: String,
    var tags: MutableList<String>,
    var batchSize: Int,
    var etaMinutes: Int,
    var timezone: String,
    var label: String,
    var status: RegionalLanePlanOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLanePlanOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLanePlanOverrideStatus): Boolean {
        when (status) {
            RegionalLanePlanOverrideStatus.DRAFT -> return next == RegionalLanePlanOverrideStatus.ACTIVE
            RegionalLanePlanOverrideStatus.PENDING -> return next == RegionalLanePlanOverrideStatus.ACTIVE
            RegionalLanePlanOverrideStatus.ACTIVE -> {
                when (next) {
                    RegionalLanePlanOverrideStatus.CLOSED -> return true
                }
                return false
            }
            RegionalLanePlanOverrideStatus.SUSPENDED -> {
                when (next) {
                    RegionalLanePlanOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalLanePlanOverrideStatus) {
        if (!canMoveTo(next)) {
            error("regional lane plan override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round(weight * batchSize.toDouble() * 100) / 100

    fun snapshot(): RegionalLanePlanOverride {
        return RegionalLanePlanOverride(
            code = code,
            name = name,
            weight = weight,
            cost = cost,
            description = description,
            tags = tags,
            batchSize = batchSize,
            etaMinutes = etaMinutes,
            timezone = timezone,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional lane plan override in status draft. */
fun newRegionalLanePlanOverride(code: String, name: String): RegionalLanePlanOverride {
    return RegionalLanePlanOverride(
        code = code,
        name = name,
        weight = 0.0,
        cost = 0.0,
        description = "",
        tags = mutableListOf<String>(),
        batchSize = 0,
        etaMinutes = 0,
        timezone = "",
        label = "",
        status = RegionalLanePlanOverrideStatus.DRAFT,
    )
}
