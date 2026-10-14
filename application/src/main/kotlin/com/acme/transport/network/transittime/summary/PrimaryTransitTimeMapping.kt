package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary transit time mapping is in its life cycle. */
enum class PrimaryTransitTimeMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary transit time mapping. Its code is unique within a tenant. */
data class PrimaryTransitTimeMapping(
    var code: String,
    var label: String,
    var segment: String,
    var stopCount: Int,
    var batchSize: Int,
    var trailerId: String,
    var visible: Boolean,
    var endsAt: Int,
    var capacity: Int,
    var maximum: Double,
    var score: Double,
    var status: PrimaryTransitTimeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTransitTimeMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTransitTimeMappingStatus): Boolean {
        when (status) {
            PrimaryTransitTimeMappingStatus.CREATED -> {
                when (next) {
                    PrimaryTransitTimeMappingStatus.VALIDATED -> return true
                }
                return false
            }
            PrimaryTransitTimeMappingStatus.VALIDATED -> return next == PrimaryTransitTimeMappingStatus.RETIRED
            PrimaryTransitTimeMappingStatus.PUBLISHED -> return next == PrimaryTransitTimeMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryTransitTimeMappingStatus) {
        if (!canMoveTo(next)) {
            error("primary transit time mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary transit time mapping"} $code"

    fun projectedLoad(): Double = round(endsAt.toDouble() * score * 100) / 100

    fun snapshot(): PrimaryTransitTimeMapping {
        return PrimaryTransitTimeMapping(
            code = code,
            label = label,
            segment = segment,
            stopCount = stopCount,
            batchSize = batchSize,
            trailerId = trailerId,
            visible = visible,
            endsAt = endsAt,
            capacity = capacity,
            maximum = maximum,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary transit time mapping in status created. */
fun newPrimaryTransitTimeMapping(code: String, label: String): PrimaryTransitTimeMapping {
    return PrimaryTransitTimeMapping(
        code = code,
        label = label,
        segment = "",
        stopCount = 0,
        batchSize = 0,
        trailerId = "",
        visible = false,
        endsAt = 0,
        capacity = 0,
        maximum = 0.0,
        score = 0.0,
        status = PrimaryTransitTimeMappingStatus.CREATED,
    )
}
