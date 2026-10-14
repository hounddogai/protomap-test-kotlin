package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan draft is in its life cycle. */
enum class LanePlanDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One lane plan draft, with what the planners need to act on it. */
data class LanePlanDraft(
    var code: String,
    var name: String,
    var unit: String,
    var timezone: String,
    var category: String,
    var rate: Double,
    var attributes: MutableMap<String, String>,
    var region: String,
    var pallets: Int,
    var etaMinutes: Int,
    var dock: String,
    var status: LanePlanDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanDraftStatus): Boolean {
        when (status) {
            LanePlanDraftStatus.PROPOSED -> return next == LanePlanDraftStatus.WITHDRAWN
            LanePlanDraftStatus.APPROVED -> return next == LanePlanDraftStatus.REJECTED || next == LanePlanDraftStatus.WITHDRAWN
            LanePlanDraftStatus.REJECTED -> {
                when (next) {
                    LanePlanDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanDraftStatus) {
        if (!canMoveTo(next)) {
            error("lane plan draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round(etaMinutes.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): LanePlanDraft {
        return LanePlanDraft(
            code = code,
            name = name,
            unit = unit,
            timezone = timezone,
            category = category,
            rate = rate,
            attributes = attributes,
            region = region,
            pallets = pallets,
            etaMinutes = etaMinutes,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lane plan draft in status proposed. */
fun newLanePlanDraft(code: String, name: String): LanePlanDraft {
    return LanePlanDraft(
        code = code,
        name = name,
        unit = "",
        timezone = "",
        category = "",
        rate = 0.0,
        attributes = mutableMapOf<String, String>(),
        region = "",
        pallets = 0,
        etaMinutes = 0,
        dock = "",
        status = LanePlanDraftStatus.PROPOSED,
    )
}
