package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.kit.text.normalize

/** Where a legacy availability template is in its life cycle. */
enum class LegacyAvailabilityTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy availability template as the availability team keeps it. */
data class LegacyAvailabilityTemplate(
    var code: String,
    var name: String,
    var ownerId: String,
    var description: String,
    var threshold: Double,
    var label: String,
    var currency: String,
    var status: LegacyAvailabilityTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAvailabilityTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAvailabilityTemplateStatus): Boolean {
        when (status) {
            LegacyAvailabilityTemplateStatus.DRAFT -> {
                when (next) {
                    LegacyAvailabilityTemplateStatus.ACTIVE -> return true
                }
                return false
            }
            LegacyAvailabilityTemplateStatus.PENDING -> {
                when (next) {
                    LegacyAvailabilityTemplateStatus.ACTIVE, LegacyAvailabilityTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyAvailabilityTemplateStatus.ACTIVE -> {
                when (next) {
                    LegacyAvailabilityTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyAvailabilityTemplateStatus.SUSPENDED -> return next == LegacyAvailabilityTemplateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LegacyAvailabilityTemplateStatus) {
        if (!canMoveTo(next)) {
            error("legacy availability template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy availability template"} $code"

    fun measuredValue(): Double = threshold

    fun snapshot(): LegacyAvailabilityTemplate {
        return LegacyAvailabilityTemplate(
            code = code,
            name = name,
            ownerId = ownerId,
            description = description,
            threshold = threshold,
            label = label,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy availability template in status draft. */
fun newLegacyAvailabilityTemplate(code: String, name: String): LegacyAvailabilityTemplate {
    return LegacyAvailabilityTemplate(
        code = code,
        name = name,
        ownerId = "",
        description = "",
        threshold = 0.0,
        label = "",
        currency = "",
        status = LegacyAvailabilityTemplateStatus.DRAFT,
    )
}
