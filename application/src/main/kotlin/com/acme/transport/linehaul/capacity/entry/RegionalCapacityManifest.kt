package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional capacity manifest is in its life cycle. */
enum class RegionalCapacityManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional capacity manifest and its current status. */
data class RegionalCapacityManifest(
    var code: String,
    var title: String,
    var windowDays: Int,
    var externalRef: String,
    var ratio: Double,
    var limit: Int,
    var source: String,
    var minimum: Double,
    var trailerId: String,
    var graceDays: Int,
    var budget: Double,
    var status: RegionalCapacityManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCapacityManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCapacityManifestStatus): Boolean {
        when (status) {
            RegionalCapacityManifestStatus.CREATED -> {
                when (next) {
                    RegionalCapacityManifestStatus.PUBLISHED -> return true
                }
                return false
            }
            RegionalCapacityManifestStatus.VALIDATED -> {
                when (next) {
                    RegionalCapacityManifestStatus.PUBLISHED, RegionalCapacityManifestStatus.RETIRED -> return true
                }
                return false
            }
            RegionalCapacityManifestStatus.PUBLISHED -> return next == RegionalCapacityManifestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalCapacityManifestStatus) {
        if (!canMoveTo(next)) {
            error("regional capacity manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveLoad(): Double = round((windowDays.toDouble() + budget) * 100) / 100

    fun snapshot(): RegionalCapacityManifest {
        return RegionalCapacityManifest(
            code = code,
            title = title,
            windowDays = windowDays,
            externalRef = externalRef,
            ratio = ratio,
            limit = limit,
            source = source,
            minimum = minimum,
            trailerId = trailerId,
            graceDays = graceDays,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional capacity manifest in status created. */
fun newRegionalCapacityManifest(code: String, title: String): RegionalCapacityManifest {
    return RegionalCapacityManifest(
        code = code,
        title = title,
        windowDays = 0,
        externalRef = "",
        ratio = 0.0,
        limit = 0,
        source = "",
        minimum = 0.0,
        trailerId = "",
        graceDays = 0,
        budget = 0.0,
        status = RegionalCapacityManifestStatus.CREATED,
    )
}
