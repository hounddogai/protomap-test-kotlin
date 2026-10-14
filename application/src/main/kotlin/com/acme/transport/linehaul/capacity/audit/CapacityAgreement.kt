package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity agreement is in its life cycle. */
enum class CapacityAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capacity agreement as the capacity team keeps it. */
data class CapacityAgreement(
    var code: String,
    var title: String,
    var graceDays: Int,
    var startsAt: Int,
    var maximum: Double,
    var phone: String,
    var status: CapacityAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityAgreementStatus): Boolean {
        when (status) {
            CapacityAgreementStatus.NEW -> return next == CapacityAgreementStatus.QUEUED
            CapacityAgreementStatus.QUEUED -> {
                when (next) {
                    CapacityAgreementStatus.RUNNING -> return true
                }
                return false
            }
            CapacityAgreementStatus.RUNNING -> {
                when (next) {
                    CapacityAgreementStatus.DONE -> return true
                }
                return false
            }
            CapacityAgreementStatus.DONE -> {
                when (next) {
                    CapacityAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityAgreementStatus) {
        if (!canMoveTo(next)) {
            error("capacity agreement $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round(graceDays.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): CapacityAgreement {
        return CapacityAgreement(
            code = code,
            title = title,
            graceDays = graceDays,
            startsAt = startsAt,
            maximum = maximum,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capacity agreement in status new. */
fun newCapacityAgreement(code: String, title: String): CapacityAgreement {
    return CapacityAgreement(
        code = code,
        title = title,
        graceDays = 0,
        startsAt = 0,
        maximum = 0.0,
        phone = "",
        status = CapacityAgreementStatus.NEW,
    )
}
