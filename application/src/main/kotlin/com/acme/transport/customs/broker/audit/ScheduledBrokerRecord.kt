package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled broker record is in its life cycle. */
enum class ScheduledBrokerRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled broker record as the broker team keeps it. */
data class ScheduledBrokerRecord(
    var code: String,
    var label: String,
    var minimum: Double,
    var shiftCode: String,
    var batchSize: Int,
    var attributes: MutableMap<String, String>,
    var status: ScheduledBrokerRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledBrokerRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledBrokerRecordStatus): Boolean {
        when (status) {
            ScheduledBrokerRecordStatus.CREATED -> return next == ScheduledBrokerRecordStatus.PUBLISHED
            ScheduledBrokerRecordStatus.VALIDATED -> return next == ScheduledBrokerRecordStatus.PUBLISHED || next == ScheduledBrokerRecordStatus.RETIRED
            ScheduledBrokerRecordStatus.PUBLISHED -> return next == ScheduledBrokerRecordStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledBrokerRecordStatus) {
        if (!canMoveTo(next)) {
            error("scheduled broker record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round(batchSize.toDouble() * minimum * 100) / 100

    fun snapshot(): ScheduledBrokerRecord {
        return ScheduledBrokerRecord(
            code = code,
            label = label,
            minimum = minimum,
            shiftCode = shiftCode,
            batchSize = batchSize,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled broker record in status created. */
fun newScheduledBrokerRecord(code: String, label: String): ScheduledBrokerRecord {
    return ScheduledBrokerRecord(
        code = code,
        label = label,
        minimum = 0.0,
        shiftCode = "",
        batchSize = 0,
        attributes = mutableMapOf<String, String>(),
        status = ScheduledBrokerRecordStatus.CREATED,
    )
}
