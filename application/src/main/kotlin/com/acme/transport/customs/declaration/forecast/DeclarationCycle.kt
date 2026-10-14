package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration cycle is in its life cycle. */
enum class DeclarationCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One declaration cycle, with what the planners need to act on it. */
data class DeclarationCycle(
    var code: String,
    var name: String,
    var externalRef: String,
    var source: String,
    var odometer: Int,
    var visible: Boolean,
    var category: String,
    var capacity: Int,
    var loadKg: Double,
    var etaMinutes: Int,
    var depot: String,
    var status: DeclarationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationCycleStatus): Boolean {
        when (status) {
            DeclarationCycleStatus.PLANNED -> return next == DeclarationCycleStatus.SCHEDULED || next == DeclarationCycleStatus.IN_PROGRESS
            DeclarationCycleStatus.SCHEDULED -> return next == DeclarationCycleStatus.IN_PROGRESS || next == DeclarationCycleStatus.CANCELLED
            DeclarationCycleStatus.IN_PROGRESS -> {
                when (next) {
                    DeclarationCycleStatus.COMPLETED, DeclarationCycleStatus.CANCELLED -> return true
                }
                return false
            }
            DeclarationCycleStatus.COMPLETED -> return next == DeclarationCycleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DeclarationCycleStatus) {
        if (!canMoveTo(next)) {
            error("declaration cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round(loadKg * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): DeclarationCycle {
        return DeclarationCycle(
            code = code,
            name = name,
            externalRef = externalRef,
            source = source,
            odometer = odometer,
            visible = visible,
            category = category,
            capacity = capacity,
            loadKg = loadKg,
            etaMinutes = etaMinutes,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a declaration cycle in status planned. */
fun newDeclarationCycle(code: String, name: String): DeclarationCycle {
    return DeclarationCycle(
        code = code,
        name = name,
        externalRef = "",
        source = "",
        odometer = 0,
        visible = false,
        category = "",
        capacity = 0,
        loadKg = 0.0,
        etaMinutes = 0,
        depot = "",
        status = DeclarationCycleStatus.PLANNED,
    )
}
