package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation template is in its life cycle. */
enum class ViolationTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A violation template as the violation team keeps it. */
data class ViolationTemplate(
    var code: String,
    var label: String,
    var timezone: String,
    var maximum: Double,
    var createdAt: Int,
    var country: String,
    var graceDays: Int,
    var cost: Double,
    var expiresAt: Int,
    var status: ViolationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationTemplateStatus): Boolean {
        when (status) {
            ViolationTemplateStatus.PROPOSED -> return next == ViolationTemplateStatus.APPROVED
            ViolationTemplateStatus.APPROVED -> return next == ViolationTemplateStatus.WITHDRAWN
            ViolationTemplateStatus.REJECTED -> {
                when (next) {
                    ViolationTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("violation template $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round(cost * expiresAt.toDouble() * 100) / 100

    fun snapshot(): ViolationTemplate {
        return ViolationTemplate(
            code = code,
            label = label,
            timezone = timezone,
            maximum = maximum,
            createdAt = createdAt,
            country = country,
            graceDays = graceDays,
            cost = cost,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a violation template in status proposed. */
fun newViolationTemplate(code: String, label: String): ViolationTemplate {
    return ViolationTemplate(
        code = code,
        label = label,
        timezone = "",
        maximum = 0.0,
        createdAt = 0,
        country = "",
        graceDays = 0,
        cost = 0.0,
        expiresAt = 0,
        status = ViolationTemplateStatus.PROPOSED,
    )
}
