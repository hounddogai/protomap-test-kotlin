package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback carbon report group is in its life cycle. */
enum class FallbackCarbonReportGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback carbon report group as the carbon report team keeps it. */
data class FallbackCarbonReportGroup(
    var code: String,
    var title: String,
    var discount: Double,
    var windowDays: Int,
    var floorValue: Double,
    var category: String,
    var minimum: Double,
    var graceDays: Int,
    var limit: Int,
    var country: String,
    var status: FallbackCarbonReportGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCarbonReportGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCarbonReportGroupStatus): Boolean {
        when (status) {
            FallbackCarbonReportGroupStatus.DRAFT -> return next == FallbackCarbonReportGroupStatus.CLOSED
            FallbackCarbonReportGroupStatus.PENDING -> return next == FallbackCarbonReportGroupStatus.ACTIVE || next == FallbackCarbonReportGroupStatus.SUSPENDED
            FallbackCarbonReportGroupStatus.ACTIVE -> {
                when (next) {
                    FallbackCarbonReportGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackCarbonReportGroupStatus.SUSPENDED -> {
                when (next) {
                    FallbackCarbonReportGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCarbonReportGroupStatus) {
        if (!canMoveTo(next)) {
            error("fallback carbon report group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round(windowDays.toDouble() * discount * 100) / 100

    fun snapshot(): FallbackCarbonReportGroup {
        return FallbackCarbonReportGroup(
            code = code,
            title = title,
            discount = discount,
            windowDays = windowDays,
            floorValue = floorValue,
            category = category,
            minimum = minimum,
            graceDays = graceDays,
            limit = limit,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback carbon report group in status draft. */
fun newFallbackCarbonReportGroup(code: String, title: String): FallbackCarbonReportGroup {
    return FallbackCarbonReportGroup(
        code = code,
        title = title,
        discount = 0.0,
        windowDays = 0,
        floorValue = 0.0,
        category = "",
        minimum = 0.0,
        graceDays = 0,
        limit = 0,
        country = "",
        status = FallbackCarbonReportGroupStatus.DRAFT,
    )
}
