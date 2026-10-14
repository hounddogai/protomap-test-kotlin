package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset rule records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset rule is in its life cycle. */
enum class RouteOffsetRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A route offset rule as the route offset team keeps it. */
data class RouteOffsetRule(
    var code: String,
    var title: String,
    var shiftCode: String,
    var notes: String,
    var label: String,
    var discount: Double,
    var currency: String,
    var locale: String,
    var tags: MutableList<String>,
    var pallets: Int,
    var attributes: MutableMap<String, String>,
    var status: RouteOffsetRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetRuleStatus): Boolean {
        when (status) {
            RouteOffsetRuleStatus.REQUESTED -> return next == RouteOffsetRuleStatus.GRANTED || next == RouteOffsetRuleStatus.REVOKED
            RouteOffsetRuleStatus.GRANTED -> return next == RouteOffsetRuleStatus.REVOKED || next == RouteOffsetRuleStatus.EXPIRED
            RouteOffsetRuleStatus.REVOKED -> {
                when (next) {
                    RouteOffsetRuleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetRuleStatus) {
        if (!canMoveTo(next)) {
            error("route offset rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

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

    fun adjustedTotal(): Double = round((discount + pallets.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetRule {
        return RouteOffsetRule(
            code = code,
            title = title,
            shiftCode = shiftCode,
            notes = notes,
            label = label,
            discount = discount,
            currency = currency,
            locale = locale,
            tags = tags,
            pallets = pallets,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a route offset rule in status requested. */
fun newRouteOffsetRule(code: String, title: String): RouteOffsetRule {
    return RouteOffsetRule(
        code = code,
        title = title,
        shiftCode = "",
        notes = "",
        label = "",
        discount = 0.0,
        currency = "",
        locale = "",
        tags = mutableListOf<String>(),
        pallets = 0,
        attributes = mutableMapOf<String, String>(),
        status = RouteOffsetRuleStatus.REQUESTED,
    )
}
