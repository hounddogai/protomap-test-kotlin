package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual fuel burn digest is in its life cycle. */
enum class ManualFuelBurnDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual fuel burn digest as the fuel burn team keeps it. */
data class ManualFuelBurnDigest(
    var code: String,
    var title: String,
    var ceiling: Double,
    var vehicleId: String,
    var priority: Int,
    var version: Int,
    var status: ManualFuelBurnDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualFuelBurnDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualFuelBurnDigestStatus): Boolean {
        when (status) {
            ManualFuelBurnDigestStatus.CREATED -> {
                when (next) {
                    ManualFuelBurnDigestStatus.VALIDATED -> return true
                }
                return false
            }
            ManualFuelBurnDigestStatus.VALIDATED -> {
                when (next) {
                    ManualFuelBurnDigestStatus.PUBLISHED, ManualFuelBurnDigestStatus.RETIRED -> return true
                }
                return false
            }
            ManualFuelBurnDigestStatus.PUBLISHED -> {
                when (next) {
                    ManualFuelBurnDigestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualFuelBurnDigestStatus) {
        if (!canMoveTo(next)) {
            error("manual fuel burn digest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun netValue(): Double = round((version.toDouble() + ceiling) * 100) / 100

    fun snapshot(): ManualFuelBurnDigest {
        return ManualFuelBurnDigest(
            code = code,
            title = title,
            ceiling = ceiling,
            vehicleId = vehicleId,
            priority = priority,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual fuel burn digest in status created. */
fun newManualFuelBurnDigest(code: String, title: String): ManualFuelBurnDigest {
    return ManualFuelBurnDigest(
        code = code,
        title = title,
        ceiling = 0.0,
        vehicleId = "",
        priority = 0,
        version = 0,
        status = ManualFuelBurnDigestStatus.CREATED,
    )
}
