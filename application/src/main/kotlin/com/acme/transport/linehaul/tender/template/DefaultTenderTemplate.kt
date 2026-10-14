package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default tender template records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a default tender template is in its life cycle. */
enum class DefaultTenderTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default tender template. Its code is unique within a tenant. */
data class DefaultTenderTemplate(
    var code: String,
    var name: String,
    var carrier: String,
    var etaMinutes: Int,
    var lane: String,
    var pinned: Boolean,
    var minimum: Double,
    var maximum: Double,
    var status: DefaultTenderTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTenderTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTenderTemplateStatus): Boolean {
        when (status) {
            DefaultTenderTemplateStatus.CREATED -> return next == DefaultTenderTemplateStatus.VALIDATED || next == DefaultTenderTemplateStatus.PUBLISHED
            DefaultTenderTemplateStatus.VALIDATED -> {
                when (next) {
                    DefaultTenderTemplateStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultTenderTemplateStatus.PUBLISHED -> {
                when (next) {
                    DefaultTenderTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultTenderTemplateStatus) {
        if (!canMoveTo(next)) {
            error("default tender template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round((etaMinutes.toDouble() - maximum) * 100) / 100

    fun snapshot(): DefaultTenderTemplate {
        return DefaultTenderTemplate(
            code = code,
            name = name,
            carrier = carrier,
            etaMinutes = etaMinutes,
            lane = lane,
            pinned = pinned,
            minimum = minimum,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default tender template in status created. */
fun newDefaultTenderTemplate(code: String, name: String): DefaultTenderTemplate {
    return DefaultTenderTemplate(
        code = code,
        name = name,
        carrier = "",
        etaMinutes = 0,
        lane = "",
        pinned = false,
        minimum = 0.0,
        maximum = 0.0,
        status = DefaultTenderTemplateStatus.CREATED,
    )
}
