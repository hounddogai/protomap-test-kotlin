package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The backhaul template records of linehaul's backhaul work, and the rules, stores, and calculations around
// them.

/** Where a backhaul template is in its life cycle. */
enum class BackhaulTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One backhaul template, with what the planners need to act on it. */
data class BackhaulTemplate(
    var code: String,
    var name: String,
    var currency: String,
    var windowDays: Int,
    var attributes: MutableMap<String, String>,
    var ceiling: Double,
    var sequence: Int,
    var ownerId: String,
    var fuelLiters: Double,
    var status: BackhaulTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulTemplateStatus): Boolean {
        when (status) {
            BackhaulTemplateStatus.DRAFT -> {
                when (next) {
                    BackhaulTemplateStatus.PENDING, BackhaulTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            BackhaulTemplateStatus.PENDING -> return next == BackhaulTemplateStatus.SUSPENDED || next == BackhaulTemplateStatus.CLOSED
            BackhaulTemplateStatus.ACTIVE -> {
                when (next) {
                    BackhaulTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            BackhaulTemplateStatus.SUSPENDED -> return next == BackhaulTemplateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BackhaulTemplateStatus) {
        if (!canMoveTo(next)) {
            error("backhaul template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"backhaul template"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round(ceiling * sequence.toDouble() * 100) / 100

    fun snapshot(): BackhaulTemplate {
        return BackhaulTemplate(
            code = code,
            name = name,
            currency = currency,
            windowDays = windowDays,
            attributes = attributes,
            ceiling = ceiling,
            sequence = sequence,
            ownerId = ownerId,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a backhaul template in status draft. */
fun newBackhaulTemplate(code: String, name: String): BackhaulTemplate {
    return BackhaulTemplate(
        code = code,
        name = name,
        currency = "",
        windowDays = 0,
        attributes = mutableMapOf<String, String>(),
        ceiling = 0.0,
        sequence = 0,
        ownerId = "",
        fuelLiters = 0.0,
        status = BackhaulTemplateStatus.DRAFT,
    )
}
