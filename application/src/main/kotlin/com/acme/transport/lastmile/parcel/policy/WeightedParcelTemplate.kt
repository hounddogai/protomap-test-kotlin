package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted parcel template is in its life cycle. */
enum class WeightedParcelTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted parcel template and its current status. */
data class WeightedParcelTemplate(
    var code: String,
    var label: String,
    var limit: Int,
    var active: Boolean,
    var score: Double,
    var discount: Double,
    var updatedAt: Int,
    var status: WeightedParcelTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedParcelTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedParcelTemplateStatus): Boolean {
        when (status) {
            WeightedParcelTemplateStatus.PLANNED -> {
                when (next) {
                    WeightedParcelTemplateStatus.IN_PROGRESS, WeightedParcelTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedParcelTemplateStatus.SCHEDULED -> return next == WeightedParcelTemplateStatus.CANCELLED
            WeightedParcelTemplateStatus.IN_PROGRESS -> return next == WeightedParcelTemplateStatus.COMPLETED
            WeightedParcelTemplateStatus.COMPLETED -> return next == WeightedParcelTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedParcelTemplateStatus) {
        if (!canMoveTo(next)) {
            error("weighted parcel template $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted parcel template"} $code"

    fun projectedVolume(): Double = round((score - limit.toDouble()) * 100) / 100

    fun snapshot(): WeightedParcelTemplate {
        return WeightedParcelTemplate(
            code = code,
            label = label,
            limit = limit,
            active = active,
            score = score,
            discount = discount,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted parcel template in status planned. */
fun newWeightedParcelTemplate(code: String, label: String): WeightedParcelTemplate {
    return WeightedParcelTemplate(
        code = code,
        label = label,
        limit = 0,
        active = false,
        score = 0.0,
        discount = 0.0,
        updatedAt = 0,
        status = WeightedParcelTemplateStatus.PLANNED,
    )
}
