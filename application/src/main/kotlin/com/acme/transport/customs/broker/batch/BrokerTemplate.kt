package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker template is in its life cycle. */
enum class BrokerTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A broker template as the broker team keeps it. */
data class BrokerTemplate(
    var code: String,
    var label: String,
    var loadKg: Double,
    var attempts: Int,
    var endsAt: Int,
    var contactEmail: String,
    var odometer: Int,
    var discount: Double,
    var status: BrokerTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerTemplateStatus): Boolean {
        when (status) {
            BrokerTemplateStatus.PLANNED -> {
                when (next) {
                    BrokerTemplateStatus.IN_PROGRESS, BrokerTemplateStatus.COMPLETED -> return true
                }
                return false
            }
            BrokerTemplateStatus.SCHEDULED -> {
                when (next) {
                    BrokerTemplateStatus.COMPLETED -> return true
                }
                return false
            }
            BrokerTemplateStatus.IN_PROGRESS -> return next == BrokerTemplateStatus.CANCELLED
            BrokerTemplateStatus.COMPLETED -> return next == BrokerTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BrokerTemplateStatus) {
        if (!canMoveTo(next)) {
            error("broker template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"broker template"} $code"

    fun weightedTotal(): Double = round(attempts.toDouble() * loadKg * 100) / 100

    fun snapshot(): BrokerTemplate {
        return BrokerTemplate(
            code = code,
            label = label,
            loadKg = loadKg,
            attempts = attempts,
            endsAt = endsAt,
            contactEmail = contactEmail,
            odometer = odometer,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a broker template in status planned. */
fun newBrokerTemplate(code: String, label: String): BrokerTemplate {
    return BrokerTemplate(
        code = code,
        label = label,
        loadKg = 0.0,
        attempts = 0,
        endsAt = 0,
        contactEmail = "",
        odometer = 0,
        discount = 0.0,
        status = BrokerTemplateStatus.PLANNED,
    )
}
