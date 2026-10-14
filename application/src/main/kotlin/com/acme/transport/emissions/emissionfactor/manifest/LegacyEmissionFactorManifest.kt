package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy emission factor manifest records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a legacy emission factor manifest is in its life cycle. */
enum class LegacyEmissionFactorManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy emission factor manifest. Its code is unique within a tenant. */
data class LegacyEmissionFactorManifest(
    var code: String,
    var name: String,
    var notes: String,
    var title: String,
    var visible: Boolean,
    var approved: Boolean,
    var description: String,
    var fuelLiters: Double,
    var vehicleId: String,
    var priority: Int,
    var windowDays: Int,
    var status: LegacyEmissionFactorManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyEmissionFactorManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyEmissionFactorManifestStatus): Boolean {
        when (status) {
            LegacyEmissionFactorManifestStatus.NEW -> return next == LegacyEmissionFactorManifestStatus.RUNNING || next == LegacyEmissionFactorManifestStatus.DONE
            LegacyEmissionFactorManifestStatus.QUEUED -> {
                when (next) {
                    LegacyEmissionFactorManifestStatus.RUNNING -> return true
                }
                return false
            }
            LegacyEmissionFactorManifestStatus.RUNNING -> return next == LegacyEmissionFactorManifestStatus.DONE
            LegacyEmissionFactorManifestStatus.DONE -> return next == LegacyEmissionFactorManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyEmissionFactorManifestStatus) {
        if (!canMoveTo(next)) {
            error("legacy emission factor manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy emission factor manifest"} $code"

    fun netTotal(): Double = round((priority.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): LegacyEmissionFactorManifest {
        return LegacyEmissionFactorManifest(
            code = code,
            name = name,
            notes = notes,
            title = title,
            visible = visible,
            approved = approved,
            description = description,
            fuelLiters = fuelLiters,
            vehicleId = vehicleId,
            priority = priority,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy emission factor manifest in status new. */
fun newLegacyEmissionFactorManifest(code: String, name: String): LegacyEmissionFactorManifest {
    return LegacyEmissionFactorManifest(
        code = code,
        name = name,
        notes = "",
        title = "",
        visible = false,
        approved = false,
        description = "",
        fuelLiters = 0.0,
        vehicleId = "",
        priority = 0,
        windowDays = 0,
        status = LegacyEmissionFactorManifestStatus.NEW,
    )
}
