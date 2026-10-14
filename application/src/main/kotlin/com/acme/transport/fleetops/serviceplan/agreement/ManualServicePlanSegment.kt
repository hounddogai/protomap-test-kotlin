package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual service plan segment is in its life cycle. */
enum class ManualServicePlanSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a manual service plan segment and its current status. */
data class ManualServicePlanSegment(
    var code: String,
    var title: String,
    var minimum: Double,
    var tags: MutableList<String>,
    var batchSize: Int,
    var cost: Double,
    var weight: Double,
    var locale: String,
    var rate: Double,
    var driverId: String,
    var status: ManualServicePlanSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualServicePlanSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualServicePlanSegmentStatus): Boolean {
        when (status) {
            ManualServicePlanSegmentStatus.NEW -> {
                when (next) {
                    ManualServicePlanSegmentStatus.FAILED -> return true
                }
                return false
            }
            ManualServicePlanSegmentStatus.QUEUED -> return next == ManualServicePlanSegmentStatus.RUNNING || next == ManualServicePlanSegmentStatus.DONE
            ManualServicePlanSegmentStatus.RUNNING -> {
                when (next) {
                    ManualServicePlanSegmentStatus.DONE, ManualServicePlanSegmentStatus.FAILED -> return true
                }
                return false
            }
            ManualServicePlanSegmentStatus.DONE -> return next == ManualServicePlanSegmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualServicePlanSegmentStatus) {
        if (!canMoveTo(next)) {
            error("manual service plan segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual service plan segment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round((rate - batchSize.toDouble()) * 100) / 100

    fun snapshot(): ManualServicePlanSegment {
        return ManualServicePlanSegment(
            code = code,
            title = title,
            minimum = minimum,
            tags = tags,
            batchSize = batchSize,
            cost = cost,
            weight = weight,
            locale = locale,
            rate = rate,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual service plan segment in status new. */
fun newManualServicePlanSegment(code: String, title: String): ManualServicePlanSegment {
    return ManualServicePlanSegment(
        code = code,
        title = title,
        minimum = 0.0,
        tags = mutableListOf<String>(),
        batchSize = 0,
        cost = 0.0,
        weight = 0.0,
        locale = "",
        rate = 0.0,
        driverId = "",
        status = ManualServicePlanSegmentStatus.NEW,
    )
}
