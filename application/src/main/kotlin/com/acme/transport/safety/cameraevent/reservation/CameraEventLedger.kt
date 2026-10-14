package com.acme.transport.safety.cameraevent.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event ledger is in its life cycle. */
enum class CameraEventLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A camera event ledger as the camera event team keeps it. */
data class CameraEventLedger(
    var code: String,
    var title: String,
    var endsAt: Int,
    var startsAt: Int,
    var driverId: String,
    var attempts: Int,
    var checksum: String,
    var visible: Boolean,
    var status: CameraEventLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventLedgerStatus): Boolean {
        when (status) {
            CameraEventLedgerStatus.NEW -> {
                when (next) {
                    CameraEventLedgerStatus.QUEUED, CameraEventLedgerStatus.DONE -> return true
                }
                return false
            }
            CameraEventLedgerStatus.QUEUED -> {
                when (next) {
                    CameraEventLedgerStatus.FAILED -> return true
                }
                return false
            }
            CameraEventLedgerStatus.RUNNING -> {
                when (next) {
                    CameraEventLedgerStatus.DONE -> return true
                }
                return false
            }
            CameraEventLedgerStatus.DONE -> return next == CameraEventLedgerStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CameraEventLedgerStatus) {
        if (!canMoveTo(next)) {
            error("camera event ledger $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round((endsAt.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): CameraEventLedger {
        return CameraEventLedger(
            code = code,
            title = title,
            endsAt = endsAt,
            startsAt = startsAt,
            driverId = driverId,
            attempts = attempts,
            checksum = checksum,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a camera event ledger in status new. */
fun newCameraEventLedger(code: String, title: String): CameraEventLedger {
    return CameraEventLedger(
        code = code,
        title = title,
        endsAt = 0,
        startsAt = 0,
        driverId = "",
        attempts = 0,
        checksum = "",
        visible = false,
        status = CameraEventLedgerStatus.NEW,
    )
}
