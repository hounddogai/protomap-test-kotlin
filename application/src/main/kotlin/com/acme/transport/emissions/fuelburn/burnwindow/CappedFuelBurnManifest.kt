package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped fuel burn manifest is in its life cycle. */
enum class CappedFuelBurnManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped fuel burn manifest, with what the planners need to act on it. */
data class CappedFuelBurnManifest(
    var code: String,
    var title: String,
    var shiftCode: String,
    var floorValue: Double,
    var weight: Double,
    var summary: String,
    var name: String,
    var currency: String,
    var expiresAt: Int,
    var label: String,
    var status: CappedFuelBurnManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedFuelBurnManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedFuelBurnManifestStatus): Boolean {
        when (status) {
            CappedFuelBurnManifestStatus.DRAFT -> {
                when (next) {
                    CappedFuelBurnManifestStatus.PENDING, CappedFuelBurnManifestStatus.ACTIVE -> return true
                }
                return false
            }
            CappedFuelBurnManifestStatus.PENDING -> return next == CappedFuelBurnManifestStatus.ACTIVE || next == CappedFuelBurnManifestStatus.SUSPENDED
            CappedFuelBurnManifestStatus.ACTIVE -> return next == CappedFuelBurnManifestStatus.SUSPENDED || next == CappedFuelBurnManifestStatus.CLOSED
            CappedFuelBurnManifestStatus.SUSPENDED -> return next == CappedFuelBurnManifestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedFuelBurnManifestStatus) {
        if (!canMoveTo(next)) {
            error("capped fuel burn manifest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$title]"

    fun projectedLoad(): Double = round((floorValue + weight) * 100) / 100

    fun snapshot(): CappedFuelBurnManifest {
        return CappedFuelBurnManifest(
            code = code,
            title = title,
            shiftCode = shiftCode,
            floorValue = floorValue,
            weight = weight,
            summary = summary,
            name = name,
            currency = currency,
            expiresAt = expiresAt,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped fuel burn manifest in status draft. */
fun newCappedFuelBurnManifest(code: String, title: String): CappedFuelBurnManifest {
    return CappedFuelBurnManifest(
        code = code,
        title = title,
        shiftCode = "",
        floorValue = 0.0,
        weight = 0.0,
        summary = "",
        name = "",
        currency = "",
        expiresAt = 0,
        label = "",
        status = CappedFuelBurnManifestStatus.DRAFT,
    )
}
