package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict backhaul mapping records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a strict backhaul mapping is in its life cycle. */
enum class StrictBackhaulMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a strict backhaul mapping and its current status. */
data class StrictBackhaulMapping(
    var code: String,
    var title: String,
    var lane: String,
    var windowDays: Int,
    var trailerId: String,
    var revision: Int,
    var createdAt: Int,
    var status: StrictBackhaulMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictBackhaulMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictBackhaulMappingStatus): Boolean {
        when (status) {
            StrictBackhaulMappingStatus.PLANNED -> {
                when (next) {
                    StrictBackhaulMappingStatus.SCHEDULED -> return true
                }
                return false
            }
            StrictBackhaulMappingStatus.SCHEDULED -> return next == StrictBackhaulMappingStatus.COMPLETED || next == StrictBackhaulMappingStatus.CANCELLED
            StrictBackhaulMappingStatus.IN_PROGRESS -> {
                when (next) {
                    StrictBackhaulMappingStatus.COMPLETED -> return true
                }
                return false
            }
            StrictBackhaulMappingStatus.COMPLETED -> {
                when (next) {
                    StrictBackhaulMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictBackhaulMappingStatus) {
        if (!canMoveTo(next)) {
            error("strict backhaul mapping $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun weightedValue(): Double = round((createdAt.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): StrictBackhaulMapping {
        return StrictBackhaulMapping(
            code = code,
            title = title,
            lane = lane,
            windowDays = windowDays,
            trailerId = trailerId,
            revision = revision,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict backhaul mapping in status planned. */
fun newStrictBackhaulMapping(code: String, title: String): StrictBackhaulMapping {
    return StrictBackhaulMapping(
        code = code,
        title = title,
        lane = "",
        windowDays = 0,
        trailerId = "",
        revision = 0,
        createdAt = 0,
        status = StrictBackhaulMappingStatus.PLANNED,
    )
}
