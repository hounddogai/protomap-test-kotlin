package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional ev charge template is in its life cycle. */
enum class RegionalEvChargeTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional ev charge template as the ev charge team keeps it. */
data class RegionalEvChargeTemplate(
    var code: String,
    var label: String,
    var trailerId: String,
    var rate: Double,
    var revision: Int,
    var visible: Boolean,
    var cost: Double,
    var attributes: MutableMap<String, String>,
    var reviewer: String,
    var timezone: String,
    var active: Boolean,
    var status: RegionalEvChargeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalEvChargeTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalEvChargeTemplateStatus): Boolean {
        when (status) {
            RegionalEvChargeTemplateStatus.DRAFT -> {
                when (next) {
                    RegionalEvChargeTemplateStatus.CLOSED -> return true
                }
                return false
            }
            RegionalEvChargeTemplateStatus.PENDING -> return next == RegionalEvChargeTemplateStatus.ACTIVE || next == RegionalEvChargeTemplateStatus.CLOSED
            RegionalEvChargeTemplateStatus.ACTIVE -> {
                when (next) {
                    RegionalEvChargeTemplateStatus.SUSPENDED, RegionalEvChargeTemplateStatus.CLOSED -> return true
                }
                return false
            }
            RegionalEvChargeTemplateStatus.SUSPENDED -> return next == RegionalEvChargeTemplateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalEvChargeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("regional ev charge template $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round(cost * revision.toDouble() * 100) / 100

    fun snapshot(): RegionalEvChargeTemplate {
        return RegionalEvChargeTemplate(
            code = code,
            label = label,
            trailerId = trailerId,
            rate = rate,
            revision = revision,
            visible = visible,
            cost = cost,
            attributes = attributes,
            reviewer = reviewer,
            timezone = timezone,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional ev charge template in status draft. */
fun newRegionalEvChargeTemplate(code: String, label: String): RegionalEvChargeTemplate {
    return RegionalEvChargeTemplate(
        code = code,
        label = label,
        trailerId = "",
        rate = 0.0,
        revision = 0,
        visible = false,
        cost = 0.0,
        attributes = mutableMapOf<String, String>(),
        reviewer = "",
        timezone = "",
        active = false,
        status = RegionalEvChargeTemplateStatus.DRAFT,
    )
}
