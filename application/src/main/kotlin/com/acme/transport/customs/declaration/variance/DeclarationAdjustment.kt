package com.acme.transport.customs.declaration.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration adjustment is in its life cycle. */
enum class DeclarationAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One declaration adjustment, with what the planners need to act on it. */
data class DeclarationAdjustment(
    var code: String,
    var label: String,
    var startsAt: Int,
    var distanceKm: Double,
    var visible: Boolean,
    var status: DeclarationAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationAdjustmentStatus): Boolean {
        when (status) {
            DeclarationAdjustmentStatus.PLANNED -> return next == DeclarationAdjustmentStatus.IN_PROGRESS || next == DeclarationAdjustmentStatus.CANCELLED
            DeclarationAdjustmentStatus.SCHEDULED -> {
                when (next) {
                    DeclarationAdjustmentStatus.COMPLETED -> return true
                }
                return false
            }
            DeclarationAdjustmentStatus.IN_PROGRESS -> return next == DeclarationAdjustmentStatus.COMPLETED || next == DeclarationAdjustmentStatus.CANCELLED
            DeclarationAdjustmentStatus.COMPLETED -> return next == DeclarationAdjustmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DeclarationAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("declaration adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round(distanceKm * startsAt.toDouble() * 100) / 100

    fun snapshot(): DeclarationAdjustment {
        return DeclarationAdjustment(
            code = code,
            label = label,
            startsAt = startsAt,
            distanceKm = distanceKm,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a declaration adjustment in status planned. */
fun newDeclarationAdjustment(code: String, label: String): DeclarationAdjustment {
    return DeclarationAdjustment(
        code = code,
        label = label,
        startsAt = 0,
        distanceKm = 0.0,
        visible = false,
        status = DeclarationAdjustmentStatus.PLANNED,
    )
}
