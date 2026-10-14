package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped carbon report group is in its life cycle. */
enum class CappedCarbonReportGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped carbon report group. Its code is unique within a tenant. */
data class CappedCarbonReportGroup(
    var code: String,
    var name: String,
    var rate: Double,
    var carrier: String,
    var endsAt: Int,
    var title: String,
    var lane: String,
    var status: CappedCarbonReportGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCarbonReportGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCarbonReportGroupStatus): Boolean {
        when (status) {
            CappedCarbonReportGroupStatus.DRAFT -> {
                when (next) {
                    CappedCarbonReportGroupStatus.ACTIVE -> return true
                }
                return false
            }
            CappedCarbonReportGroupStatus.PENDING -> {
                when (next) {
                    CappedCarbonReportGroupStatus.ACTIVE -> return true
                }
                return false
            }
            CappedCarbonReportGroupStatus.ACTIVE -> {
                when (next) {
                    CappedCarbonReportGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedCarbonReportGroupStatus.SUSPENDED -> return next == CappedCarbonReportGroupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedCarbonReportGroupStatus) {
        if (!canMoveTo(next)) {
            error("capped carbon report group $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netTotal(): Double = round((rate - endsAt.toDouble()) * 100) / 100

    fun snapshot(): CappedCarbonReportGroup {
        return CappedCarbonReportGroup(
            code = code,
            name = name,
            rate = rate,
            carrier = carrier,
            endsAt = endsAt,
            title = title,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped carbon report group in status draft. */
fun newCappedCarbonReportGroup(code: String, name: String): CappedCarbonReportGroup {
    return CappedCarbonReportGroup(
        code = code,
        name = name,
        rate = 0.0,
        carrier = "",
        endsAt = 0,
        title = "",
        lane = "",
        status = CappedCarbonReportGroupStatus.DRAFT,
    )
}
