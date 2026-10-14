package com.acme.transport.terminal.sortation.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation template records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation template is in its life cycle. */
enum class SortationTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A sortation template. Its code is unique within a tenant. */
data class SortationTemplate(
    var code: String,
    var label: String,
    var routeCode: String,
    var discount: Double,
    var depot: String,
    var ceiling: Double,
    var updatedAt: Int,
    var fuelLiters: Double,
    var minimum: Double,
    var status: SortationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationTemplateStatus): Boolean {
        when (status) {
            SortationTemplateStatus.DRAFT -> {
                when (next) {
                    SortationTemplateStatus.ACTIVE, SortationTemplateStatus.CLOSED -> return true
                }
                return false
            }
            SortationTemplateStatus.PENDING -> return next == SortationTemplateStatus.ACTIVE || next == SortationTemplateStatus.SUSPENDED
            SortationTemplateStatus.ACTIVE -> return next == SortationTemplateStatus.SUSPENDED || next == SortationTemplateStatus.CLOSED
            SortationTemplateStatus.SUSPENDED -> return next == SortationTemplateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SortationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("sortation template $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedLoad(): Double = round((fuelLiters + minimum) * 100) / 100

    fun snapshot(): SortationTemplate {
        return SortationTemplate(
            code = code,
            label = label,
            routeCode = routeCode,
            discount = discount,
            depot = depot,
            ceiling = ceiling,
            updatedAt = updatedAt,
            fuelLiters = fuelLiters,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a sortation template in status draft. */
fun newSortationTemplate(code: String, label: String): SortationTemplate {
    return SortationTemplate(
        code = code,
        label = label,
        routeCode = "",
        discount = 0.0,
        depot = "",
        ceiling = 0.0,
        updatedAt = 0,
        fuelLiters = 0.0,
        minimum = 0.0,
        status = SortationTemplateStatus.DRAFT,
    )
}
