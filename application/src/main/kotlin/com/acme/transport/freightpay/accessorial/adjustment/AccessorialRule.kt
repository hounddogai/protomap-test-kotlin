package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.text.normalize

/** Where a accessorial rule is in its life cycle. */
enum class AccessorialRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a accessorial rule and its current status. */
data class AccessorialRule(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var currency: String,
    var durationMinutes: Int,
    var category: String,
    var locale: String,
    var label: String,
    var summary: String,
    var status: AccessorialRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialRuleStatus): Boolean {
        when (status) {
            AccessorialRuleStatus.PLANNED -> {
                when (next) {
                    AccessorialRuleStatus.COMPLETED, AccessorialRuleStatus.CANCELLED -> return true
                }
                return false
            }
            AccessorialRuleStatus.SCHEDULED -> return next == AccessorialRuleStatus.IN_PROGRESS
            AccessorialRuleStatus.IN_PROGRESS -> return next == AccessorialRuleStatus.CANCELLED
            AccessorialRuleStatus.COMPLETED -> return next == AccessorialRuleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AccessorialRuleStatus) {
        if (!canMoveTo(next)) {
            error("accessorial rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"accessorial rule"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): AccessorialRule {
        return AccessorialRule(
            code = code,
            title = title,
            tags = tags,
            currency = currency,
            durationMinutes = durationMinutes,
            category = category,
            locale = locale,
            label = label,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a accessorial rule in status planned. */
fun newAccessorialRule(code: String, title: String): AccessorialRule {
    return AccessorialRule(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        currency = "",
        durationMinutes = 0,
        category = "",
        locale = "",
        label = "",
        summary = "",
        status = AccessorialRuleStatus.PLANNED,
    )
}
