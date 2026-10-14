package com.acme.transport.terminal.sortation.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict sortation manifest is in its life cycle. */
enum class StrictSortationManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict sortation manifest as the sortation team keeps it. */
data class StrictSortationManifest(
    var code: String,
    var label: String,
    var summary: String,
    var phone: String,
    var driverId: String,
    var limit: Int,
    var name: String,
    var maximum: Double,
    var threshold: Double,
    var region: String,
    var status: StrictSortationManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictSortationManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictSortationManifestStatus): Boolean {
        when (status) {
            StrictSortationManifestStatus.NEW -> return next == StrictSortationManifestStatus.QUEUED || next == StrictSortationManifestStatus.RUNNING
            StrictSortationManifestStatus.QUEUED -> {
                when (next) {
                    StrictSortationManifestStatus.FAILED -> return true
                }
                return false
            }
            StrictSortationManifestStatus.RUNNING -> {
                when (next) {
                    StrictSortationManifestStatus.FAILED -> return true
                }
                return false
            }
            StrictSortationManifestStatus.DONE -> return next == StrictSortationManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictSortationManifestStatus) {
        if (!canMoveTo(next)) {
            error("strict sortation manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedTotal(): Double = round((limit.toDouble() - maximum) * 100) / 100

    fun snapshot(): StrictSortationManifest {
        return StrictSortationManifest(
            code = code,
            label = label,
            summary = summary,
            phone = phone,
            driverId = driverId,
            limit = limit,
            name = name,
            maximum = maximum,
            threshold = threshold,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict sortation manifest in status new. */
fun newStrictSortationManifest(code: String, label: String): StrictSortationManifest {
    return StrictSortationManifest(
        code = code,
        label = label,
        summary = "",
        phone = "",
        driverId = "",
        limit = 0,
        name = "",
        maximum = 0.0,
        threshold = 0.0,
        region = "",
        status = StrictSortationManifestStatus.NEW,
    )
}
