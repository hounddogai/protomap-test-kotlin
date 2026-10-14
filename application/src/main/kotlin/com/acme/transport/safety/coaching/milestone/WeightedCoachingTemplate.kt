package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted coaching template is in its life cycle. */
enum class WeightedCoachingTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted coaching template. Its code is unique within a tenant. */
data class WeightedCoachingTemplate(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var score: Double,
    var checksum: String,
    var version: Int,
    var externalRef: String,
    var source: String,
    var weight: Double,
    var odometer: Int,
    var status: WeightedCoachingTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCoachingTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCoachingTemplateStatus): Boolean {
        when (status) {
            WeightedCoachingTemplateStatus.DRAFT -> {
                when (next) {
                    WeightedCoachingTemplateStatus.SUSPENDED, WeightedCoachingTemplateStatus.CLOSED -> return true
                }
                return false
            }
            WeightedCoachingTemplateStatus.PENDING -> {
                when (next) {
                    WeightedCoachingTemplateStatus.ACTIVE, WeightedCoachingTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedCoachingTemplateStatus.ACTIVE -> {
                when (next) {
                    WeightedCoachingTemplateStatus.SUSPENDED, WeightedCoachingTemplateStatus.CLOSED -> return true
                }
                return false
            }
            WeightedCoachingTemplateStatus.SUSPENDED -> {
                when (next) {
                    WeightedCoachingTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCoachingTemplateStatus) {
        if (!canMoveTo(next)) {
            error("weighted coaching template $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round(durationMinutes.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): WeightedCoachingTemplate {
        return WeightedCoachingTemplate(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            score = score,
            checksum = checksum,
            version = version,
            externalRef = externalRef,
            source = source,
            weight = weight,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted coaching template in status draft. */
fun newWeightedCoachingTemplate(code: String, name: String): WeightedCoachingTemplate {
    return WeightedCoachingTemplate(
        code = code,
        name = name,
        durationMinutes = 0,
        score = 0.0,
        checksum = "",
        version = 0,
        externalRef = "",
        source = "",
        weight = 0.0,
        odometer = 0,
        status = WeightedCoachingTemplateStatus.DRAFT,
    )
}
