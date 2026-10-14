package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.kit.text.normalize

// The global service plan preference records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a global service plan preference is in its life cycle. */
enum class GlobalServicePlanPreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global service plan preference, with what the planners need to act on it. */
data class GlobalServicePlanPreference(
    var code: String,
    var label: String,
    var visible: Boolean,
    var region: String,
    var approved: Boolean,
    var name: String,
    var score: Double,
    var status: GlobalServicePlanPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalServicePlanPreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalServicePlanPreferenceStatus): Boolean {
        when (status) {
            GlobalServicePlanPreferenceStatus.CREATED -> return next == GlobalServicePlanPreferenceStatus.VALIDATED || next == GlobalServicePlanPreferenceStatus.RETIRED
            GlobalServicePlanPreferenceStatus.VALIDATED -> {
                when (next) {
                    GlobalServicePlanPreferenceStatus.PUBLISHED, GlobalServicePlanPreferenceStatus.RETIRED -> return true
                }
                return false
            }
            GlobalServicePlanPreferenceStatus.PUBLISHED -> {
                when (next) {
                    GlobalServicePlanPreferenceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalServicePlanPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("global service plan preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = score

    fun snapshot(): GlobalServicePlanPreference {
        return GlobalServicePlanPreference(
            code = code,
            label = label,
            visible = visible,
            region = region,
            approved = approved,
            name = name,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global service plan preference in status created. */
fun newGlobalServicePlanPreference(code: String, label: String): GlobalServicePlanPreference {
    return GlobalServicePlanPreference(
        code = code,
        label = label,
        visible = false,
        region = "",
        approved = false,
        name = "",
        score = 0.0,
        status = GlobalServicePlanPreferenceStatus.CREATED,
    )
}
