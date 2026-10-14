package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification policy is in its life cycle. */
enum class QualificationPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A qualification policy. Its code is unique within a tenant. */
data class QualificationPolicy(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var ceiling: Double,
    var createdAt: Int,
    var driverId: String,
    var endsAt: Int,
    var loadKg: Double,
    var version: Int,
    var status: QualificationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationPolicyStatus): Boolean {
        when (status) {
            QualificationPolicyStatus.IDLE -> {
                when (next) {
                    QualificationPolicyStatus.STOPPED -> return true
                }
                return false
            }
            QualificationPolicyStatus.WARMING -> {
                when (next) {
                    QualificationPolicyStatus.READY, QualificationPolicyStatus.DRAINING -> return true
                }
                return false
            }
            QualificationPolicyStatus.READY -> {
                when (next) {
                    QualificationPolicyStatus.DRAINING, QualificationPolicyStatus.STOPPED -> return true
                }
                return false
            }
            QualificationPolicyStatus.DRAINING -> return next == QualificationPolicyStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: QualificationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("qualification policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round((distanceKm + ceiling) * 100) / 100

    fun snapshot(): QualificationPolicy {
        return QualificationPolicy(
            code = code,
            label = label,
            distanceKm = distanceKm,
            ceiling = ceiling,
            createdAt = createdAt,
            driverId = driverId,
            endsAt = endsAt,
            loadKg = loadKg,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a qualification policy in status idle. */
fun newQualificationPolicy(code: String, label: String): QualificationPolicy {
    return QualificationPolicy(
        code = code,
        label = label,
        distanceKm = 0.0,
        ceiling = 0.0,
        createdAt = 0,
        driverId = "",
        endsAt = 0,
        loadKg = 0.0,
        version = 0,
        status = QualificationPolicyStatus.IDLE,
    )
}
