package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual dwell manifest is in its life cycle. */
enum class ManualDwellManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual dwell manifest as the dwell team keeps it. */
data class ManualDwellManifest(
    var code: String,
    var name: String,
    var capacity: Int,
    var pinned: Boolean,
    var weight: Double,
    var approved: Boolean,
    var source: String,
    var contactEmail: String,
    var lane: String,
    var fuelLiters: Double,
    var status: ManualDwellManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDwellManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDwellManifestStatus): Boolean {
        when (status) {
            ManualDwellManifestStatus.PROPOSED -> return next == ManualDwellManifestStatus.WITHDRAWN
            ManualDwellManifestStatus.APPROVED -> {
                when (next) {
                    ManualDwellManifestStatus.REJECTED, ManualDwellManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualDwellManifestStatus.REJECTED -> return next == ManualDwellManifestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ManualDwellManifestStatus) {
        if (!canMoveTo(next)) {
            error("manual dwell manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round((weight + capacity.toDouble()) * 100) / 100

    fun snapshot(): ManualDwellManifest {
        return ManualDwellManifest(
            code = code,
            name = name,
            capacity = capacity,
            pinned = pinned,
            weight = weight,
            approved = approved,
            source = source,
            contactEmail = contactEmail,
            lane = lane,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual dwell manifest in status proposed. */
fun newManualDwellManifest(code: String, name: String): ManualDwellManifest {
    return ManualDwellManifest(
        code = code,
        name = name,
        capacity = 0,
        pinned = false,
        weight = 0.0,
        approved = false,
        source = "",
        contactEmail = "",
        lane = "",
        fuelLiters = 0.0,
        status = ManualDwellManifestStatus.PROPOSED,
    )
}
