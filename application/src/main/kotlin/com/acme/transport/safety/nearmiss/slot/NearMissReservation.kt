package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss reservation is in its life cycle. */
enum class NearMissReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A near miss reservation as the near miss team keeps it. */
data class NearMissReservation(
    var code: String,
    var label: String,
    var discount: Double,
    var retries: Int,
    var score: Double,
    var depot: String,
    var startsAt: Int,
    var visible: Boolean,
    var ceiling: Double,
    var capacity: Int,
    var status: NearMissReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissReservationStatus): Boolean {
        when (status) {
            NearMissReservationStatus.DRAFT -> {
                when (next) {
                    NearMissReservationStatus.CLOSED -> return true
                }
                return false
            }
            NearMissReservationStatus.PENDING -> return next == NearMissReservationStatus.ACTIVE
            NearMissReservationStatus.ACTIVE -> {
                when (next) {
                    NearMissReservationStatus.SUSPENDED, NearMissReservationStatus.CLOSED -> return true
                }
                return false
            }
            NearMissReservationStatus.SUSPENDED -> {
                when (next) {
                    NearMissReservationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissReservationStatus) {
        if (!canMoveTo(next)) {
            error("near miss reservation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round((ceiling - startsAt.toDouble()) * 100) / 100

    fun snapshot(): NearMissReservation {
        return NearMissReservation(
            code = code,
            label = label,
            discount = discount,
            retries = retries,
            score = score,
            depot = depot,
            startsAt = startsAt,
            visible = visible,
            ceiling = ceiling,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a near miss reservation in status draft. */
fun newNearMissReservation(code: String, label: String): NearMissReservation {
    return NearMissReservation(
        code = code,
        label = label,
        discount = 0.0,
        retries = 0,
        score = 0.0,
        depot = "",
        startsAt = 0,
        visible = false,
        ceiling = 0.0,
        capacity = 0,
        status = NearMissReservationStatus.DRAFT,
    )
}
