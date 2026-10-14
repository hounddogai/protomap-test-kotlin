package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional idling mapping is in its life cycle. */
enum class RegionalIdlingMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional idling mapping. Its code is unique within a tenant. */
data class RegionalIdlingMapping(
    var code: String,
    var title: String,
    var segment: String,
    var quantity: Int,
    var pinned: Boolean,
    var lane: String,
    var source: String,
    var sequence: Int,
    var distanceKm: Double,
    var endsAt: Int,
    var status: RegionalIdlingMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIdlingMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIdlingMappingStatus): Boolean {
        when (status) {
            RegionalIdlingMappingStatus.IDLE -> return next == RegionalIdlingMappingStatus.READY
            RegionalIdlingMappingStatus.WARMING -> {
                when (next) {
                    RegionalIdlingMappingStatus.READY -> return true
                }
                return false
            }
            RegionalIdlingMappingStatus.READY -> return next == RegionalIdlingMappingStatus.DRAINING
            RegionalIdlingMappingStatus.DRAINING -> {
                when (next) {
                    RegionalIdlingMappingStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalIdlingMappingStatus) {
        if (!canMoveTo(next)) {
            error("regional idling mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedLoad(): Double = round((distanceKm - sequence.toDouble()) * 100) / 100

    fun snapshot(): RegionalIdlingMapping {
        return RegionalIdlingMapping(
            code = code,
            title = title,
            segment = segment,
            quantity = quantity,
            pinned = pinned,
            lane = lane,
            source = source,
            sequence = sequence,
            distanceKm = distanceKm,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional idling mapping in status idle. */
fun newRegionalIdlingMapping(code: String, title: String): RegionalIdlingMapping {
    return RegionalIdlingMapping(
        code = code,
        title = title,
        segment = "",
        quantity = 0,
        pinned = false,
        lane = "",
        source = "",
        sequence = 0,
        distanceKm = 0.0,
        endsAt = 0,
        status = RegionalIdlingMappingStatus.IDLE,
    )
}
