package com.acme.transport.customs.broker.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped broker record is in its life cycle. */
enum class CappedBrokerRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped broker record. Its code is unique within a tenant. */
data class CappedBrokerRecord(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var etaMinutes: Int,
    var trailerId: String,
    var locale: String,
    var dock: String,
    var unit: String,
    var retries: Int,
    var distanceKm: Double,
    var quantity: Int,
    var status: CappedBrokerRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBrokerRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBrokerRecordStatus): Boolean {
        when (status) {
            CappedBrokerRecordStatus.DRAFT -> return next == CappedBrokerRecordStatus.PENDING
            CappedBrokerRecordStatus.PENDING -> return next == CappedBrokerRecordStatus.CLOSED
            CappedBrokerRecordStatus.ACTIVE -> {
                when (next) {
                    CappedBrokerRecordStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedBrokerRecordStatus.SUSPENDED -> {
                when (next) {
                    CappedBrokerRecordStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedBrokerRecordStatus) {
        if (!canMoveTo(next)) {
            error("capped broker record $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round((distanceKm + quantity.toDouble()) * 100) / 100

    fun snapshot(): CappedBrokerRecord {
        return CappedBrokerRecord(
            code = code,
            title = title,
            updatedAt = updatedAt,
            etaMinutes = etaMinutes,
            trailerId = trailerId,
            locale = locale,
            dock = dock,
            unit = unit,
            retries = retries,
            distanceKm = distanceKm,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped broker record in status draft. */
fun newCappedBrokerRecord(code: String, title: String): CappedBrokerRecord {
    return CappedBrokerRecord(
        code = code,
        title = title,
        updatedAt = 0,
        etaMinutes = 0,
        trailerId = "",
        locale = "",
        dock = "",
        unit = "",
        retries = 0,
        distanceKm = 0.0,
        quantity = 0,
        status = CappedBrokerRecordStatus.DRAFT,
    )
}
