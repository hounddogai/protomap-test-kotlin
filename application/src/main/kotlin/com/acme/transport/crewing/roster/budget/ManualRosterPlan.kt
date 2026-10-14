package com.acme.transport.crewing.roster.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual roster plan is in its life cycle. */
enum class ManualRosterPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One manual roster plan, with what the planners need to act on it. */
data class ManualRosterPlan(
    var code: String,
    var title: String,
    var routeCode: String,
    var attributes: MutableMap<String, String>,
    var graceDays: Int,
    var createdAt: Int,
    var startsAt: Int,
    var etaMinutes: Int,
    var retries: Int,
    var tags: MutableList<String>,
    var weight: Double,
    var status: ManualRosterPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRosterPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRosterPlanStatus): Boolean {
        when (status) {
            ManualRosterPlanStatus.CREATED -> {
                when (next) {
                    ManualRosterPlanStatus.VALIDATED, ManualRosterPlanStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualRosterPlanStatus.VALIDATED -> return next == ManualRosterPlanStatus.PUBLISHED || next == ManualRosterPlanStatus.RETIRED
            ManualRosterPlanStatus.PUBLISHED -> return next == ManualRosterPlanStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualRosterPlanStatus) {
        if (!canMoveTo(next)) {
            error("manual roster plan $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"manual roster plan"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((startsAt.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): ManualRosterPlan {
        return ManualRosterPlan(
            code = code,
            title = title,
            routeCode = routeCode,
            attributes = attributes,
            graceDays = graceDays,
            createdAt = createdAt,
            startsAt = startsAt,
            etaMinutes = etaMinutes,
            retries = retries,
            tags = tags,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual roster plan in status created. */
fun newManualRosterPlan(code: String, title: String): ManualRosterPlan {
    return ManualRosterPlan(
        code = code,
        title = title,
        routeCode = "",
        attributes = mutableMapOf<String, String>(),
        graceDays = 0,
        createdAt = 0,
        startsAt = 0,
        etaMinutes = 0,
        retries = 0,
        tags = mutableListOf<String>(),
        weight = 0.0,
        status = ManualRosterPlanStatus.CREATED,
    )
}
