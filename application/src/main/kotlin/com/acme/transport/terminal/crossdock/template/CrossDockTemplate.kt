package com.acme.transport.terminal.crossdock.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cross dock template records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a cross dock template is in its life cycle. */
enum class CrossDockTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A cross dock template. Its code is unique within a tenant. */
data class CrossDockTemplate(
    var code: String,
    var name: String,
    var unit: String,
    var locale: String,
    var depot: String,
    var title: String,
    var pinned: Boolean,
    var maximum: Double,
    var pallets: Int,
    var minimum: Double,
    var status: CrossDockTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockTemplateStatus): Boolean {
        when (status) {
            CrossDockTemplateStatus.DRAFT -> return next == CrossDockTemplateStatus.CLOSED
            CrossDockTemplateStatus.PENDING -> return next == CrossDockTemplateStatus.CLOSED
            CrossDockTemplateStatus.ACTIVE -> return next == CrossDockTemplateStatus.SUSPENDED || next == CrossDockTemplateStatus.CLOSED
            CrossDockTemplateStatus.SUSPENDED -> {
                when (next) {
                    CrossDockTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockTemplateStatus) {
        if (!canMoveTo(next)) {
            error("cross dock template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round(minimum * maximum * 100) / 100

    fun snapshot(): CrossDockTemplate {
        return CrossDockTemplate(
            code = code,
            name = name,
            unit = unit,
            locale = locale,
            depot = depot,
            title = title,
            pinned = pinned,
            maximum = maximum,
            pallets = pallets,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a cross dock template in status draft. */
fun newCrossDockTemplate(code: String, name: String): CrossDockTemplate {
    return CrossDockTemplate(
        code = code,
        name = name,
        unit = "",
        locale = "",
        depot = "",
        title = "",
        pinned = false,
        maximum = 0.0,
        pallets = 0,
        minimum = 0.0,
        status = CrossDockTemplateStatus.DRAFT,
    )
}
