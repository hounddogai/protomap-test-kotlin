package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.text.normalize

/** Where a default rate card cycle is in its life cycle. */
enum class DefaultRateCardCycleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default rate card cycle. Its code is unique within a tenant. */
data class DefaultRateCardCycle(
    var code: String,
    var name: String,
    var contactEmail: String,
    var active: Boolean,
    var currency: String,
    var source: String,
    var batchSize: Int,
    var unit: String,
    var status: DefaultRateCardCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRateCardCycleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRateCardCycleStatus): Boolean {
        when (status) {
            DefaultRateCardCycleStatus.NEW -> return next == DefaultRateCardCycleStatus.DONE || next == DefaultRateCardCycleStatus.FAILED
            DefaultRateCardCycleStatus.QUEUED -> {
                when (next) {
                    DefaultRateCardCycleStatus.RUNNING -> return true
                }
                return false
            }
            DefaultRateCardCycleStatus.RUNNING -> {
                when (next) {
                    DefaultRateCardCycleStatus.DONE, DefaultRateCardCycleStatus.FAILED -> return true
                }
                return false
            }
            DefaultRateCardCycleStatus.DONE -> {
                when (next) {
                    DefaultRateCardCycleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRateCardCycleStatus) {
        if (!canMoveTo(next)) {
            error("default rate card cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): DefaultRateCardCycle {
        return DefaultRateCardCycle(
            code = code,
            name = name,
            contactEmail = contactEmail,
            active = active,
            currency = currency,
            source = source,
            batchSize = batchSize,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default rate card cycle in status new. */
fun newDefaultRateCardCycle(code: String, name: String): DefaultRateCardCycle {
    return DefaultRateCardCycle(
        code = code,
        name = name,
        contactEmail = "",
        active = false,
        currency = "",
        source = "",
        batchSize = 0,
        unit = "",
        status = DefaultRateCardCycleStatus.NEW,
    )
}
