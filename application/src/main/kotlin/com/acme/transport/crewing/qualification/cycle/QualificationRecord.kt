package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification record is in its life cycle. */
enum class QualificationRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A qualification record. Its code is unique within a tenant. */
data class QualificationRecord(
    var code: String,
    var name: String,
    var loadKg: Double,
    var retries: Int,
    var distanceKm: Double,
    var lane: String,
    var version: Int,
    var discount: Double,
    var currency: String,
    var createdAt: Int,
    var status: QualificationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationRecordStatus): Boolean {
        when (status) {
            QualificationRecordStatus.REQUESTED -> {
                when (next) {
                    QualificationRecordStatus.REVOKED, QualificationRecordStatus.EXPIRED -> return true
                }
                return false
            }
            QualificationRecordStatus.GRANTED -> return next == QualificationRecordStatus.EXPIRED
            QualificationRecordStatus.REVOKED -> {
                when (next) {
                    QualificationRecordStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationRecordStatus) {
        if (!canMoveTo(next)) {
            error("qualification record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun effectiveValue(): Double = round((version.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): QualificationRecord {
        return QualificationRecord(
            code = code,
            name = name,
            loadKg = loadKg,
            retries = retries,
            distanceKm = distanceKm,
            lane = lane,
            version = version,
            discount = discount,
            currency = currency,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a qualification record in status requested. */
fun newQualificationRecord(code: String, name: String): QualificationRecord {
    return QualificationRecord(
        code = code,
        name = name,
        loadKg = 0.0,
        retries = 0,
        distanceKm = 0.0,
        lane = "",
        version = 0,
        discount = 0.0,
        currency = "",
        createdAt = 0,
        status = QualificationRecordStatus.REQUESTED,
    )
}
