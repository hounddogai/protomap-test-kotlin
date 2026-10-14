package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area record is in its life cycle. */
enum class ServiceAreaRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A service area record as the service area team keeps it. */
data class ServiceAreaRecord(
    var code: String,
    var title: String,
    var amount: Double,
    var rate: Double,
    var capacity: Int,
    var channel: String,
    var ratio: Double,
    var status: ServiceAreaRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaRecordStatus): Boolean {
        when (status) {
            ServiceAreaRecordStatus.REQUESTED -> {
                when (next) {
                    ServiceAreaRecordStatus.EXPIRED -> return true
                }
                return false
            }
            ServiceAreaRecordStatus.GRANTED -> return next == ServiceAreaRecordStatus.EXPIRED
            ServiceAreaRecordStatus.REVOKED -> return next == ServiceAreaRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ServiceAreaRecordStatus) {
        if (!canMoveTo(next)) {
            error("service area record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round((capacity.toDouble() + rate) * 100) / 100

    fun snapshot(): ServiceAreaRecord {
        return ServiceAreaRecord(
            code = code,
            title = title,
            amount = amount,
            rate = rate,
            capacity = capacity,
            channel = channel,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a service area record in status requested. */
fun newServiceAreaRecord(code: String, title: String): ServiceAreaRecord {
    return ServiceAreaRecord(
        code = code,
        title = title,
        amount = 0.0,
        rate = 0.0,
        capacity = 0,
        channel = "",
        ratio = 0.0,
        status = ServiceAreaRecordStatus.REQUESTED,
    )
}
