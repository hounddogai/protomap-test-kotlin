package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire adjustment is in its life cycle. */
enum class TireAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tire adjustment as the tire team keeps it. */
data class TireAdjustment(
    var code: String,
    var label: String,
    var limit: Int,
    var name: String,
    var loadKg: Double,
    var status: TireAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireAdjustmentStatus): Boolean {
        when (status) {
            TireAdjustmentStatus.CREATED -> return next == TireAdjustmentStatus.VALIDATED || next == TireAdjustmentStatus.PUBLISHED
            TireAdjustmentStatus.VALIDATED -> {
                when (next) {
                    TireAdjustmentStatus.PUBLISHED, TireAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
            TireAdjustmentStatus.PUBLISHED -> return next == TireAdjustmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TireAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("tire adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netVolume(): Double = round((loadKg - limit.toDouble()) * 100) / 100

    fun snapshot(): TireAdjustment {
        return TireAdjustment(
            code = code,
            label = label,
            limit = limit,
            name = name,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tire adjustment in status created. */
fun newTireAdjustment(code: String, label: String): TireAdjustment {
    return TireAdjustment(
        code = code,
        label = label,
        limit = 0,
        name = "",
        loadKg = 0.0,
        status = TireAdjustmentStatus.CREATED,
    )
}
