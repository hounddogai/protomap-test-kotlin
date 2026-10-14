package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event agreement is in its life cycle. */
enum class CameraEventAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A camera event agreement. Its code is unique within a tenant. */
data class CameraEventAgreement(
    var code: String,
    var label: String,
    var amount: Double,
    var endsAt: Int,
    var routeCode: String,
    var ceiling: Double,
    var status: CameraEventAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventAgreementStatus): Boolean {
        when (status) {
            CameraEventAgreementStatus.NEW -> {
                when (next) {
                    CameraEventAgreementStatus.DONE, CameraEventAgreementStatus.FAILED -> return true
                }
                return false
            }
            CameraEventAgreementStatus.QUEUED -> {
                when (next) {
                    CameraEventAgreementStatus.DONE, CameraEventAgreementStatus.FAILED -> return true
                }
                return false
            }
            CameraEventAgreementStatus.RUNNING -> return next == CameraEventAgreementStatus.DONE || next == CameraEventAgreementStatus.FAILED
            CameraEventAgreementStatus.DONE -> {
                when (next) {
                    CameraEventAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CameraEventAgreementStatus) {
        if (!canMoveTo(next)) {
            error("camera event agreement $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveVolume(): Double = round((ceiling - endsAt.toDouble()) * 100) / 100

    fun snapshot(): CameraEventAgreement {
        return CameraEventAgreement(
            code = code,
            label = label,
            amount = amount,
            endsAt = endsAt,
            routeCode = routeCode,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a camera event agreement in status new. */
fun newCameraEventAgreement(code: String, label: String): CameraEventAgreement {
    return CameraEventAgreement(
        code = code,
        label = label,
        amount = 0.0,
        endsAt = 0,
        routeCode = "",
        ceiling = 0.0,
        status = CameraEventAgreementStatus.NEW,
    )
}
