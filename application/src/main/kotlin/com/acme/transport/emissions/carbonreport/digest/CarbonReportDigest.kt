package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The carbon report digest records of emissions's carbon report work, and the rules, stores, and calculations
// around them.

/** Where a carbon report digest is in its life cycle. */
enum class CarbonReportDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a carbon report digest and its current status. */
data class CarbonReportDigest(
    var code: String,
    var title: String,
    var trailerId: String,
    var fuelLiters: Double,
    var distanceKm: Double,
    var category: String,
    var depot: String,
    var discount: Double,
    var revision: Int,
    var dock: String,
    var attributes: MutableMap<String, String>,
    var status: CarbonReportDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportDigestStatus): Boolean {
        when (status) {
            CarbonReportDigestStatus.IDLE -> return next == CarbonReportDigestStatus.READY
            CarbonReportDigestStatus.WARMING -> return next == CarbonReportDigestStatus.READY
            CarbonReportDigestStatus.READY -> return next == CarbonReportDigestStatus.DRAINING
            CarbonReportDigestStatus.DRAINING -> return next == CarbonReportDigestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CarbonReportDigestStatus) {
        if (!canMoveTo(next)) {
            error("carbon report digest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round((revision.toDouble() - discount) * 100) / 100

    fun snapshot(): CarbonReportDigest {
        return CarbonReportDigest(
            code = code,
            title = title,
            trailerId = trailerId,
            fuelLiters = fuelLiters,
            distanceKm = distanceKm,
            category = category,
            depot = depot,
            discount = discount,
            revision = revision,
            dock = dock,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a carbon report digest in status idle. */
fun newCarbonReportDigest(code: String, title: String): CarbonReportDigest {
    return CarbonReportDigest(
        code = code,
        title = title,
        trailerId = "",
        fuelLiters = 0.0,
        distanceKm = 0.0,
        category = "",
        depot = "",
        discount = 0.0,
        revision = 0,
        dock = "",
        attributes = mutableMapOf<String, String>(),
        status = CarbonReportDigestStatus.IDLE,
    )
}
