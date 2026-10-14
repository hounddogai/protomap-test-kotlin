package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional carbon report manifest is in its life cycle. */
enum class RegionalCarbonReportManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional carbon report manifest, with what the planners need to act on it. */
data class RegionalCarbonReportManifest(
    var code: String,
    var title: String,
    var pallets: Int,
    var version: Int,
    var carrier: String,
    var trailerId: String,
    var score: Double,
    var threshold: Double,
    var source: String,
    var endsAt: Int,
    var status: RegionalCarbonReportManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCarbonReportManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCarbonReportManifestStatus): Boolean {
        when (status) {
            RegionalCarbonReportManifestStatus.NEW -> return next == RegionalCarbonReportManifestStatus.QUEUED || next == RegionalCarbonReportManifestStatus.RUNNING
            RegionalCarbonReportManifestStatus.QUEUED -> return next == RegionalCarbonReportManifestStatus.DONE
            RegionalCarbonReportManifestStatus.RUNNING -> {
                when (next) {
                    RegionalCarbonReportManifestStatus.FAILED -> return true
                }
                return false
            }
            RegionalCarbonReportManifestStatus.DONE -> return next == RegionalCarbonReportManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalCarbonReportManifestStatus) {
        if (!canMoveTo(next)) {
            error("regional carbon report manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun projectedVolume(): Double = round(threshold * score * 100) / 100

    fun snapshot(): RegionalCarbonReportManifest {
        return RegionalCarbonReportManifest(
            code = code,
            title = title,
            pallets = pallets,
            version = version,
            carrier = carrier,
            trailerId = trailerId,
            score = score,
            threshold = threshold,
            source = source,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional carbon report manifest in status new. */
fun newRegionalCarbonReportManifest(code: String, title: String): RegionalCarbonReportManifest {
    return RegionalCarbonReportManifest(
        code = code,
        title = title,
        pallets = 0,
        version = 0,
        carrier = "",
        trailerId = "",
        score = 0.0,
        threshold = 0.0,
        source = "",
        endsAt = 0,
        status = RegionalCarbonReportManifestStatus.NEW,
    )
}
