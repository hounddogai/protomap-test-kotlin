package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation manifest is in its life cycle. */
enum class SortationManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a sortation manifest and its current status. */
data class SortationManifest(
    var code: String,
    var label: String,
    var odometer: Int,
    var maximum: Double,
    var tags: MutableList<String>,
    var shiftCode: String,
    var dock: String,
    var threshold: Double,
    var vehicleId: String,
    var title: String,
    var status: SortationManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationManifestStatus): Boolean {
        when (status) {
            SortationManifestStatus.CREATED -> return next == SortationManifestStatus.VALIDATED
            SortationManifestStatus.VALIDATED -> {
                when (next) {
                    SortationManifestStatus.PUBLISHED, SortationManifestStatus.RETIRED -> return true
                }
                return false
            }
            SortationManifestStatus.PUBLISHED -> return next == SortationManifestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SortationManifestStatus) {
        if (!canMoveTo(next)) {
            error("sortation manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round((threshold + odometer.toDouble()) * 100) / 100

    fun snapshot(): SortationManifest {
        return SortationManifest(
            code = code,
            label = label,
            odometer = odometer,
            maximum = maximum,
            tags = tags,
            shiftCode = shiftCode,
            dock = dock,
            threshold = threshold,
            vehicleId = vehicleId,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a sortation manifest in status created. */
fun newSortationManifest(code: String, label: String): SortationManifest {
    return SortationManifest(
        code = code,
        label = label,
        odometer = 0,
        maximum = 0.0,
        tags = mutableListOf<String>(),
        shiftCode = "",
        dock = "",
        threshold = 0.0,
        vehicleId = "",
        title = "",
        status = SortationManifestStatus.CREATED,
    )
}
