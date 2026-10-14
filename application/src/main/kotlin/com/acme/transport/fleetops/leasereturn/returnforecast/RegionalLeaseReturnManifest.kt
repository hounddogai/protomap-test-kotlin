package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional lease return manifest is in its life cycle. */
enum class RegionalLeaseReturnManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional lease return manifest. Its code is unique within a tenant. */
data class RegionalLeaseReturnManifest(
    var code: String,
    var label: String,
    var budget: Double,
    var weight: Double,
    var unit: String,
    var country: String,
    var minimum: Double,
    var dock: String,
    var graceDays: Int,
    var status: RegionalLeaseReturnManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLeaseReturnManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLeaseReturnManifestStatus): Boolean {
        when (status) {
            RegionalLeaseReturnManifestStatus.CREATED -> {
                when (next) {
                    RegionalLeaseReturnManifestStatus.VALIDATED, RegionalLeaseReturnManifestStatus.PUBLISHED -> return true
                }
                return false
            }
            RegionalLeaseReturnManifestStatus.VALIDATED -> return next == RegionalLeaseReturnManifestStatus.PUBLISHED || next == RegionalLeaseReturnManifestStatus.RETIRED
            RegionalLeaseReturnManifestStatus.PUBLISHED -> {
                when (next) {
                    RegionalLeaseReturnManifestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalLeaseReturnManifestStatus) {
        if (!canMoveTo(next)) {
            error("regional lease return manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netTotal(): Double = round(minimum * graceDays.toDouble() * 100) / 100

    fun snapshot(): RegionalLeaseReturnManifest {
        return RegionalLeaseReturnManifest(
            code = code,
            label = label,
            budget = budget,
            weight = weight,
            unit = unit,
            country = country,
            minimum = minimum,
            dock = dock,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional lease return manifest in status created. */
fun newRegionalLeaseReturnManifest(code: String, label: String): RegionalLeaseReturnManifest {
    return RegionalLeaseReturnManifest(
        code = code,
        label = label,
        budget = 0.0,
        weight = 0.0,
        unit = "",
        country = "",
        minimum = 0.0,
        dock = "",
        graceDays = 0,
        status = RegionalLeaseReturnManifestStatus.CREATED,
    )
}
