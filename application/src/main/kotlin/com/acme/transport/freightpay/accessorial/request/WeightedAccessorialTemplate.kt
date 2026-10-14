package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted accessorial template is in its life cycle. */
enum class WeightedAccessorialTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a weighted accessorial template and its current status. */
data class WeightedAccessorialTemplate(
    var code: String,
    var name: String,
    var trailerId: String,
    var expiresAt: Int,
    var segment: String,
    var ceiling: Double,
    var limit: Int,
    var status: WeightedAccessorialTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAccessorialTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAccessorialTemplateStatus): Boolean {
        when (status) {
            WeightedAccessorialTemplateStatus.DRAFT -> return next == WeightedAccessorialTemplateStatus.SUSPENDED || next == WeightedAccessorialTemplateStatus.CLOSED
            WeightedAccessorialTemplateStatus.PENDING -> {
                when (next) {
                    WeightedAccessorialTemplateStatus.CLOSED -> return true
                }
                return false
            }
            WeightedAccessorialTemplateStatus.ACTIVE -> {
                when (next) {
                    WeightedAccessorialTemplateStatus.SUSPENDED, WeightedAccessorialTemplateStatus.CLOSED -> return true
                }
                return false
            }
            WeightedAccessorialTemplateStatus.SUSPENDED -> {
                when (next) {
                    WeightedAccessorialTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedAccessorialTemplateStatus) {
        if (!canMoveTo(next)) {
            error("weighted accessorial template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"weighted accessorial template"} $code"

    fun netExposure(): Double = round((expiresAt.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): WeightedAccessorialTemplate {
        return WeightedAccessorialTemplate(
            code = code,
            name = name,
            trailerId = trailerId,
            expiresAt = expiresAt,
            segment = segment,
            ceiling = ceiling,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted accessorial template in status draft. */
fun newWeightedAccessorialTemplate(code: String, name: String): WeightedAccessorialTemplate {
    return WeightedAccessorialTemplate(
        code = code,
        name = name,
        trailerId = "",
        expiresAt = 0,
        segment = "",
        ceiling = 0.0,
        limit = 0,
        status = WeightedAccessorialTemplateStatus.DRAFT,
    )
}
