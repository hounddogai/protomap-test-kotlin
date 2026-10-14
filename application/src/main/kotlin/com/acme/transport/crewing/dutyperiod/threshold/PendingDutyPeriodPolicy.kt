package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending duty period policy is in its life cycle. */
enum class PendingDutyPeriodPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a pending duty period policy and its current status. */
data class PendingDutyPeriodPolicy(
    var code: String,
    var title: String,
    var vehicleId: String,
    var shiftCode: String,
    var phone: String,
    var attributes: MutableMap<String, String>,
    var ratio: Double,
    var quantity: Int,
    var dock: String,
    var stopCount: Int,
    var status: PendingDutyPeriodPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDutyPeriodPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDutyPeriodPolicyStatus): Boolean {
        when (status) {
            PendingDutyPeriodPolicyStatus.NEW -> {
                when (next) {
                    PendingDutyPeriodPolicyStatus.QUEUED, PendingDutyPeriodPolicyStatus.RUNNING -> return true
                }
                return false
            }
            PendingDutyPeriodPolicyStatus.QUEUED -> {
                when (next) {
                    PendingDutyPeriodPolicyStatus.RUNNING, PendingDutyPeriodPolicyStatus.FAILED -> return true
                }
                return false
            }
            PendingDutyPeriodPolicyStatus.RUNNING -> return next == PendingDutyPeriodPolicyStatus.DONE
            PendingDutyPeriodPolicyStatus.DONE -> return next == PendingDutyPeriodPolicyStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingDutyPeriodPolicyStatus) {
        if (!canMoveTo(next)) {
            error("pending duty period policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((quantity.toDouble() + ratio) * 100) / 100

    fun snapshot(): PendingDutyPeriodPolicy {
        return PendingDutyPeriodPolicy(
            code = code,
            title = title,
            vehicleId = vehicleId,
            shiftCode = shiftCode,
            phone = phone,
            attributes = attributes,
            ratio = ratio,
            quantity = quantity,
            dock = dock,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending duty period policy in status new. */
fun newPendingDutyPeriodPolicy(code: String, title: String): PendingDutyPeriodPolicy {
    return PendingDutyPeriodPolicy(
        code = code,
        title = title,
        vehicleId = "",
        shiftCode = "",
        phone = "",
        attributes = mutableMapOf<String, String>(),
        ratio = 0.0,
        quantity = 0,
        dock = "",
        stopCount = 0,
        status = PendingDutyPeriodPolicyStatus.NEW,
    )
}
