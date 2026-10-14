package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient camera event ledger is in its life cycle. */
enum class LenientCameraEventLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient camera event ledger as the camera event team keeps it. */
data class LenientCameraEventLedger(
    var code: String,
    var label: String,
    var ratio: Double,
    var trailerId: String,
    var notes: String,
    var visible: Boolean,
    var checksum: String,
    var country: String,
    var tags: MutableList<String>,
    var quantity: Int,
    var vehicleId: String,
    var status: LenientCameraEventLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCameraEventLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCameraEventLedgerStatus): Boolean {
        when (status) {
            LenientCameraEventLedgerStatus.PLANNED -> return next == LenientCameraEventLedgerStatus.COMPLETED || next == LenientCameraEventLedgerStatus.CANCELLED
            LenientCameraEventLedgerStatus.SCHEDULED -> {
                when (next) {
                    LenientCameraEventLedgerStatus.COMPLETED, LenientCameraEventLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            LenientCameraEventLedgerStatus.IN_PROGRESS -> return next == LenientCameraEventLedgerStatus.COMPLETED || next == LenientCameraEventLedgerStatus.CANCELLED
            LenientCameraEventLedgerStatus.COMPLETED -> return next == LenientCameraEventLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientCameraEventLedgerStatus) {
        if (!canMoveTo(next)) {
            error("lenient camera event ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lenient camera event ledger"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round((quantity.toDouble() - ratio) * 100) / 100

    fun snapshot(): LenientCameraEventLedger {
        return LenientCameraEventLedger(
            code = code,
            label = label,
            ratio = ratio,
            trailerId = trailerId,
            notes = notes,
            visible = visible,
            checksum = checksum,
            country = country,
            tags = tags,
            quantity = quantity,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient camera event ledger in status planned. */
fun newLenientCameraEventLedger(code: String, label: String): LenientCameraEventLedger {
    return LenientCameraEventLedger(
        code = code,
        label = label,
        ratio = 0.0,
        trailerId = "",
        notes = "",
        visible = false,
        checksum = "",
        country = "",
        tags = mutableListOf<String>(),
        quantity = 0,
        vehicleId = "",
        status = LenientCameraEventLedgerStatus.PLANNED,
    )
}
