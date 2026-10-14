package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled capacity agreement is in its life cycle. */
enum class ScheduledCapacityAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scheduled capacity agreement, with what the planners need to act on it. */
data class ScheduledCapacityAgreement(
    var code: String,
    var title: String,
    var createdAt: Int,
    var loadKg: Double,
    var batchSize: Int,
    var region: String,
    var currency: String,
    var status: ScheduledCapacityAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCapacityAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCapacityAgreementStatus): Boolean {
        when (status) {
            ScheduledCapacityAgreementStatus.NEW -> {
                when (next) {
                    ScheduledCapacityAgreementStatus.QUEUED, ScheduledCapacityAgreementStatus.FAILED -> return true
                }
                return false
            }
            ScheduledCapacityAgreementStatus.QUEUED -> return next == ScheduledCapacityAgreementStatus.FAILED
            ScheduledCapacityAgreementStatus.RUNNING -> return next == ScheduledCapacityAgreementStatus.DONE || next == ScheduledCapacityAgreementStatus.FAILED
            ScheduledCapacityAgreementStatus.DONE -> return next == ScheduledCapacityAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledCapacityAgreementStatus) {
        if (!canMoveTo(next)) {
            error("scheduled capacity agreement $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedExposure(): Double = round((loadKg - createdAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledCapacityAgreement {
        return ScheduledCapacityAgreement(
            code = code,
            title = title,
            createdAt = createdAt,
            loadKg = loadKg,
            batchSize = batchSize,
            region = region,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled capacity agreement in status new. */
fun newScheduledCapacityAgreement(code: String, title: String): ScheduledCapacityAgreement {
    return ScheduledCapacityAgreement(
        code = code,
        title = title,
        createdAt = 0,
        loadKg = 0.0,
        batchSize = 0,
        region = "",
        currency = "",
        status = ScheduledCapacityAgreementStatus.NEW,
    )
}
