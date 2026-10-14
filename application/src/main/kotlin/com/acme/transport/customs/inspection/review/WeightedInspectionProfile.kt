package com.acme.transport.customs.inspection.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted inspection profile is in its life cycle. */
enum class WeightedInspectionProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted inspection profile. Its code is unique within a tenant. */
data class WeightedInspectionProfile(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var externalRef: String,
    var depot: String,
    var ceiling: Double,
    var status: WeightedInspectionProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedInspectionProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedInspectionProfileStatus): Boolean {
        when (status) {
            WeightedInspectionProfileStatus.DRAFT -> {
                when (next) {
                    WeightedInspectionProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedInspectionProfileStatus.PENDING -> {
                when (next) {
                    WeightedInspectionProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedInspectionProfileStatus.ACTIVE -> return next == WeightedInspectionProfileStatus.SUSPENDED
            WeightedInspectionProfileStatus.SUSPENDED -> {
                when (next) {
                    WeightedInspectionProfileStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedInspectionProfileStatus) {
        if (!canMoveTo(next)) {
            error("weighted inspection profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round((etaMinutes.toDouble() - ceiling) * 100) / 100

    fun snapshot(): WeightedInspectionProfile {
        return WeightedInspectionProfile(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            externalRef = externalRef,
            depot = depot,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted inspection profile in status draft. */
fun newWeightedInspectionProfile(code: String, name: String): WeightedInspectionProfile {
    return WeightedInspectionProfile(
        code = code,
        name = name,
        etaMinutes = 0,
        externalRef = "",
        depot = "",
        ceiling = 0.0,
        status = WeightedInspectionProfileStatus.DRAFT,
    )
}
