package com.acme.transport.customs.broker.reservation

import com.acme.transport.kit.text.normalize

/** Where a broker record is in its life cycle. */
enum class BrokerRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One broker record, with what the planners need to act on it. */
data class BrokerRecord(
    var code: String,
    var label: String,
    var shiftCode: String,
    var title: String,
    var segment: String,
    var region: String,
    var quantity: Int,
    var status: BrokerRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerRecordStatus): Boolean {
        when (status) {
            BrokerRecordStatus.NEW -> return next == BrokerRecordStatus.DONE
            BrokerRecordStatus.QUEUED -> {
                when (next) {
                    BrokerRecordStatus.RUNNING, BrokerRecordStatus.DONE -> return true
                }
                return false
            }
            BrokerRecordStatus.RUNNING -> return next == BrokerRecordStatus.DONE || next == BrokerRecordStatus.FAILED
            BrokerRecordStatus.DONE -> return next == BrokerRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BrokerRecordStatus) {
        if (!canMoveTo(next)) {
            error("broker record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"broker record"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): BrokerRecord {
        return BrokerRecord(
            code = code,
            label = label,
            shiftCode = shiftCode,
            title = title,
            segment = segment,
            region = region,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a broker record in status new. */
fun newBrokerRecord(code: String, label: String): BrokerRecord {
    return BrokerRecord(
        code = code,
        label = label,
        shiftCode = "",
        title = "",
        segment = "",
        region = "",
        quantity = 0,
        status = BrokerRecordStatus.NEW,
    )
}
