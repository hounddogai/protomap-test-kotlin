package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy declaration profile is in its life cycle. */
enum class LegacyDeclarationProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy declaration profile as the declaration team keeps it. */
data class LegacyDeclarationProfile(
    var code: String,
    var label: String,
    var limit: Int,
    var revision: Int,
    var distanceKm: Double,
    var vehicleId: String,
    var currency: String,
    var routeCode: String,
    var ownerId: String,
    var status: LegacyDeclarationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDeclarationProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDeclarationProfileStatus): Boolean {
        when (status) {
            LegacyDeclarationProfileStatus.PLANNED -> {
                when (next) {
                    LegacyDeclarationProfileStatus.SCHEDULED, LegacyDeclarationProfileStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyDeclarationProfileStatus.SCHEDULED -> {
                when (next) {
                    LegacyDeclarationProfileStatus.IN_PROGRESS, LegacyDeclarationProfileStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyDeclarationProfileStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyDeclarationProfileStatus.COMPLETED, LegacyDeclarationProfileStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyDeclarationProfileStatus.COMPLETED -> return next == LegacyDeclarationProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyDeclarationProfileStatus) {
        if (!canMoveTo(next)) {
            error("legacy declaration profile $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round((revision.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): LegacyDeclarationProfile {
        return LegacyDeclarationProfile(
            code = code,
            label = label,
            limit = limit,
            revision = revision,
            distanceKm = distanceKm,
            vehicleId = vehicleId,
            currency = currency,
            routeCode = routeCode,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy declaration profile in status planned. */
fun newLegacyDeclarationProfile(code: String, label: String): LegacyDeclarationProfile {
    return LegacyDeclarationProfile(
        code = code,
        label = label,
        limit = 0,
        revision = 0,
        distanceKm = 0.0,
        vehicleId = "",
        currency = "",
        routeCode = "",
        ownerId = "",
        status = LegacyDeclarationProfileStatus.PLANNED,
    )
}
