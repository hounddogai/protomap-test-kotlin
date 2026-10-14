package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff reservation is in its life cycle. */
enum class TariffReservationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a tariff reservation and its current status. */
data class TariffReservation(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var odometer: Int,
    var endsAt: Int,
    var etaMinutes: Int,
    var status: TariffReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffReservationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffReservationStatus): Boolean {
        when (status) {
            TariffReservationStatus.CREATED -> {
                when (next) {
                    TariffReservationStatus.RETIRED -> return true
                }
                return false
            }
            TariffReservationStatus.VALIDATED -> {
                when (next) {
                    TariffReservationStatus.PUBLISHED -> return true
                }
                return false
            }
            TariffReservationStatus.PUBLISHED -> {
                when (next) {
                    TariffReservationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffReservationStatus) {
        if (!canMoveTo(next)) {
            error("tariff reservation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((endsAt.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TariffReservation {
        return TariffReservation(
            code = code,
            name = name,
            expiresAt = expiresAt,
            odometer = odometer,
            endsAt = endsAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tariff reservation in status created. */
fun newTariffReservation(code: String, name: String): TariffReservation {
    return TariffReservation(
        code = code,
        name = name,
        expiresAt = 0,
        odometer = 0,
        endsAt = 0,
        etaMinutes = 0,
        status = TariffReservationStatus.CREATED,
    )
}
