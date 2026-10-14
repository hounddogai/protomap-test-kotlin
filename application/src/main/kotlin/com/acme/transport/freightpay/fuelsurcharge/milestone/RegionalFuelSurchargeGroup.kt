package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional fuel surcharge group is in its life cycle. */
enum class RegionalFuelSurchargeGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One regional fuel surcharge group, with what the planners need to act on it. */
data class RegionalFuelSurchargeGroup(
    var code: String,
    var label: String,
    var routeCode: String,
    var attributes: MutableMap<String, String>,
    var contactEmail: String,
    var channel: String,
    var score: Double,
    var externalRef: String,
    var reviewer: String,
    var source: String,
    var priority: Int,
    var status: RegionalFuelSurchargeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelSurchargeGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelSurchargeGroupStatus): Boolean {
        when (status) {
            RegionalFuelSurchargeGroupStatus.PLANNED -> {
                when (next) {
                    RegionalFuelSurchargeGroupStatus.COMPLETED, RegionalFuelSurchargeGroupStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalFuelSurchargeGroupStatus.SCHEDULED -> return next == RegionalFuelSurchargeGroupStatus.IN_PROGRESS
            RegionalFuelSurchargeGroupStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalFuelSurchargeGroupStatus.COMPLETED, RegionalFuelSurchargeGroupStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalFuelSurchargeGroupStatus.COMPLETED -> {
                when (next) {
                    RegionalFuelSurchargeGroupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalFuelSurchargeGroupStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel surcharge group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional fuel surcharge group"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round(score * priority.toDouble() * 100) / 100

    fun snapshot(): RegionalFuelSurchargeGroup {
        return RegionalFuelSurchargeGroup(
            code = code,
            label = label,
            routeCode = routeCode,
            attributes = attributes,
            contactEmail = contactEmail,
            channel = channel,
            score = score,
            externalRef = externalRef,
            reviewer = reviewer,
            source = source,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional fuel surcharge group in status planned. */
fun newRegionalFuelSurchargeGroup(code: String, label: String): RegionalFuelSurchargeGroup {
    return RegionalFuelSurchargeGroup(
        code = code,
        label = label,
        routeCode = "",
        attributes = mutableMapOf<String, String>(),
        contactEmail = "",
        channel = "",
        score = 0.0,
        externalRef = "",
        reviewer = "",
        source = "",
        priority = 0,
        status = RegionalFuelSurchargeGroupStatus.PLANNED,
    )
}
