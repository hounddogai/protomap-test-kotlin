package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.normalize

/** Where a camera event quota is in its life cycle. */
enum class CameraEventQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A camera event quota as the camera event team keeps it. */
data class CameraEventQuota(
    var code: String,
    var name: String,
    var externalRef: String,
    var source: String,
    var reviewer: String,
    var lane: String,
    var phone: String,
    var score: Double,
    var status: CameraEventQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventQuotaStatus): Boolean {
        when (status) {
            CameraEventQuotaStatus.DRAFT -> return next == CameraEventQuotaStatus.ACTIVE || next == CameraEventQuotaStatus.SUSPENDED
            CameraEventQuotaStatus.PENDING -> {
                when (next) {
                    CameraEventQuotaStatus.SUSPENDED, CameraEventQuotaStatus.CLOSED -> return true
                }
                return false
            }
            CameraEventQuotaStatus.ACTIVE -> {
                when (next) {
                    CameraEventQuotaStatus.SUSPENDED -> return true
                }
                return false
            }
            CameraEventQuotaStatus.SUSPENDED -> return next == CameraEventQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CameraEventQuotaStatus) {
        if (!canMoveTo(next)) {
            error("camera event quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = score

    fun snapshot(): CameraEventQuota {
        return CameraEventQuota(
            code = code,
            name = name,
            externalRef = externalRef,
            source = source,
            reviewer = reviewer,
            lane = lane,
            phone = phone,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a camera event quota in status draft. */
fun newCameraEventQuota(code: String, name: String): CameraEventQuota {
    return CameraEventQuota(
        code = code,
        name = name,
        externalRef = "",
        source = "",
        reviewer = "",
        lane = "",
        phone = "",
        score = 0.0,
        status = CameraEventQuotaStatus.DRAFT,
    )
}
