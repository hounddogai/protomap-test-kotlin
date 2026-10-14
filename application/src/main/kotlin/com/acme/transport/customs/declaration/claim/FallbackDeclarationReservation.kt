package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback declaration reservation is in its life cycle. */
enum class FallbackDeclarationReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fallback declaration reservation, with what the planners need to act on it. */
data class FallbackDeclarationReservation(
    var code: String,
    var name: String,
    var phone: String,
    var threshold: Double,
    var notes: String,
    var endsAt: Int,
    var driverId: String,
    var etaMinutes: Int,
    var version: Int,
    var status: FallbackDeclarationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDeclarationReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDeclarationReservationStatus): Boolean {
        when (status) {
            FallbackDeclarationReservationStatus.IDLE -> {
                when (next) {
                    FallbackDeclarationReservationStatus.WARMING -> return true
                }
                return false
            }
            FallbackDeclarationReservationStatus.WARMING -> {
                when (next) {
                    FallbackDeclarationReservationStatus.DRAINING -> return true
                }
                return false
            }
            FallbackDeclarationReservationStatus.READY -> return next == FallbackDeclarationReservationStatus.DRAINING || next == FallbackDeclarationReservationStatus.STOPPED
            FallbackDeclarationReservationStatus.DRAINING -> {
                when (next) {
                    FallbackDeclarationReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDeclarationReservationStatus) {
        if (!canMoveTo(next)) {
            error("fallback declaration reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $name"

    fun projectedLoad(): Double = round((etaMinutes.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): FallbackDeclarationReservation {
        return FallbackDeclarationReservation(
            code = code,
            name = name,
            phone = phone,
            threshold = threshold,
            notes = notes,
            endsAt = endsAt,
            driverId = driverId,
            etaMinutes = etaMinutes,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback declaration reservation in status idle. */
fun newFallbackDeclarationReservation(code: String, name: String): FallbackDeclarationReservation {
    return FallbackDeclarationReservation(
        code = code,
        name = name,
        phone = "",
        threshold = 0.0,
        notes = "",
        endsAt = 0,
        driverId = "",
        etaMinutes = 0,
        version = 0,
        status = FallbackDeclarationReservationStatus.IDLE,
    )
}
