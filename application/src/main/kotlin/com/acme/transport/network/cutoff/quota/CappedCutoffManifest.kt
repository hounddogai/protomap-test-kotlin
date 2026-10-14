package com.acme.transport.network.cutoff.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped cutoff manifest is in its life cycle. */
enum class CappedCutoffManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped cutoff manifest as the cutoff team keeps it. */
data class CappedCutoffManifest(
    var code: String,
    var name: String,
    var limit: Int,
    var updatedAt: Int,
    var ownerId: String,
    var visible: Boolean,
    var driverId: String,
    var odometer: Int,
    var loadKg: Double,
    var description: String,
    var status: CappedCutoffManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCutoffManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCutoffManifestStatus): Boolean {
        when (status) {
            CappedCutoffManifestStatus.NEW -> {
                when (next) {
                    CappedCutoffManifestStatus.QUEUED, CappedCutoffManifestStatus.RUNNING -> return true
                }
                return false
            }
            CappedCutoffManifestStatus.QUEUED -> return next == CappedCutoffManifestStatus.FAILED
            CappedCutoffManifestStatus.RUNNING -> {
                when (next) {
                    CappedCutoffManifestStatus.DONE -> return true
                }
                return false
            }
            CappedCutoffManifestStatus.DONE -> {
                when (next) {
                    CappedCutoffManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCutoffManifestStatus) {
        if (!canMoveTo(next)) {
            error("capped cutoff manifest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round(odometer.toDouble() * loadKg * 100) / 100

    fun snapshot(): CappedCutoffManifest {
        return CappedCutoffManifest(
            code = code,
            name = name,
            limit = limit,
            updatedAt = updatedAt,
            ownerId = ownerId,
            visible = visible,
            driverId = driverId,
            odometer = odometer,
            loadKg = loadKg,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped cutoff manifest in status new. */
fun newCappedCutoffManifest(code: String, name: String): CappedCutoffManifest {
    return CappedCutoffManifest(
        code = code,
        name = name,
        limit = 0,
        updatedAt = 0,
        ownerId = "",
        visible = false,
        driverId = "",
        odometer = 0,
        loadKg = 0.0,
        description = "",
        status = CappedCutoffManifestStatus.NEW,
    )
}
