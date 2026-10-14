package com.acme.transport.emissions.evcharge.milestone

import com.acme.transport.kit.text.normalize

/** Where a seasonal ev charge audit is in its life cycle. */
enum class SeasonalEvChargeAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal ev charge audit as the ev charge team keeps it. */
data class SeasonalEvChargeAudit(
    var code: String,
    var label: String,
    var category: String,
    var limit: Int,
    var attributes: MutableMap<String, String>,
    var tags: MutableList<String>,
    var externalRef: String,
    var approved: Boolean,
    var active: Boolean,
    var status: SeasonalEvChargeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEvChargeAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEvChargeAuditStatus): Boolean {
        when (status) {
            SeasonalEvChargeAuditStatus.IDLE -> return next == SeasonalEvChargeAuditStatus.WARMING || next == SeasonalEvChargeAuditStatus.DRAINING
            SeasonalEvChargeAuditStatus.WARMING -> return next == SeasonalEvChargeAuditStatus.STOPPED
            SeasonalEvChargeAuditStatus.READY -> return next == SeasonalEvChargeAuditStatus.DRAINING
            SeasonalEvChargeAuditStatus.DRAINING -> {
                when (next) {
                    SeasonalEvChargeAuditStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalEvChargeAuditStatus) {
        if (!canMoveTo(next)) {
            error("seasonal ev charge audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal ev charge audit"} $code"

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

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): SeasonalEvChargeAudit {
        return SeasonalEvChargeAudit(
            code = code,
            label = label,
            category = category,
            limit = limit,
            attributes = attributes,
            tags = tags,
            externalRef = externalRef,
            approved = approved,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal ev charge audit in status idle. */
fun newSeasonalEvChargeAudit(code: String, label: String): SeasonalEvChargeAudit {
    return SeasonalEvChargeAudit(
        code = code,
        label = label,
        category = "",
        limit = 0,
        attributes = mutableMapOf<String, String>(),
        tags = mutableListOf<String>(),
        externalRef = "",
        approved = false,
        active = false,
        status = SeasonalEvChargeAuditStatus.IDLE,
    )
}
