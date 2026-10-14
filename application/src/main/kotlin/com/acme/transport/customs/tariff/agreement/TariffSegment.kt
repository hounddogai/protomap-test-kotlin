package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff segment is in its life cycle. */
enum class TariffSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tariff segment as the tariff team keeps it. */
data class TariffSegment(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var unit: String,
    var maximum: Double,
    var distanceKm: Double,
    var source: String,
    var status: TariffSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffSegmentStatus): Boolean {
        when (status) {
            TariffSegmentStatus.IDLE -> return next == TariffSegmentStatus.STOPPED
            TariffSegmentStatus.WARMING -> return next == TariffSegmentStatus.READY || next == TariffSegmentStatus.DRAINING
            TariffSegmentStatus.READY -> return next == TariffSegmentStatus.DRAINING
            TariffSegmentStatus.DRAINING -> {
                when (next) {
                    TariffSegmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffSegmentStatus) {
        if (!canMoveTo(next)) {
            error("tariff segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round(distanceKm * maximum * 100) / 100

    fun snapshot(): TariffSegment {
        return TariffSegment(
            code = code,
            title = title,
            tags = tags,
            unit = unit,
            maximum = maximum,
            distanceKm = distanceKm,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tariff segment in status idle. */
fun newTariffSegment(code: String, title: String): TariffSegment {
    return TariffSegment(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        unit = "",
        maximum = 0.0,
        distanceKm = 0.0,
        source = "",
        status = TariffSegmentStatus.IDLE,
    )
}
