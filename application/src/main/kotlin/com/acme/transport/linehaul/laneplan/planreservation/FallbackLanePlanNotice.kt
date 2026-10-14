package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback lane plan notice is in its life cycle. */
enum class FallbackLanePlanNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback lane plan notice. Its code is unique within a tenant. */
data class FallbackLanePlanNotice(
    var code: String,
    var title: String,
    var timezone: String,
    var ratio: Double,
    var floorValue: Double,
    var maximum: Double,
    var trailerId: String,
    var depot: String,
    var name: String,
    var status: FallbackLanePlanNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLanePlanNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLanePlanNoticeStatus): Boolean {
        when (status) {
            FallbackLanePlanNoticeStatus.NEW -> {
                when (next) {
                    FallbackLanePlanNoticeStatus.DONE, FallbackLanePlanNoticeStatus.FAILED -> return true
                }
                return false
            }
            FallbackLanePlanNoticeStatus.QUEUED -> return next == FallbackLanePlanNoticeStatus.DONE
            FallbackLanePlanNoticeStatus.RUNNING -> {
                when (next) {
                    FallbackLanePlanNoticeStatus.DONE, FallbackLanePlanNoticeStatus.FAILED -> return true
                }
                return false
            }
            FallbackLanePlanNoticeStatus.DONE -> {
                when (next) {
                    FallbackLanePlanNoticeStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackLanePlanNoticeStatus) {
        if (!canMoveTo(next)) {
            error("fallback lane plan notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedExposure(): Double = round((maximum - ratio) * 100) / 100

    fun snapshot(): FallbackLanePlanNotice {
        return FallbackLanePlanNotice(
            code = code,
            title = title,
            timezone = timezone,
            ratio = ratio,
            floorValue = floorValue,
            maximum = maximum,
            trailerId = trailerId,
            depot = depot,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback lane plan notice in status new. */
fun newFallbackLanePlanNotice(code: String, title: String): FallbackLanePlanNotice {
    return FallbackLanePlanNotice(
        code = code,
        title = title,
        timezone = "",
        ratio = 0.0,
        floorValue = 0.0,
        maximum = 0.0,
        trailerId = "",
        depot = "",
        name = "",
        status = FallbackLanePlanNoticeStatus.NEW,
    )
}
