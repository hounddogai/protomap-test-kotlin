package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional camera event record is in its life cycle. */
enum class RegionalCameraEventRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional camera event record and its current status. */
data class RegionalCameraEventRecord(
    var code: String,
    var title: String,
    var margin: Double,
    var quantity: Int,
    var attributes: MutableMap<String, String>,
    var sequence: Int,
    var stopCount: Int,
    var createdAt: Int,
    var discount: Double,
    var budget: Double,
    var batchSize: Int,
    var status: RegionalCameraEventRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCameraEventRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCameraEventRecordStatus): Boolean {
        when (status) {
            RegionalCameraEventRecordStatus.CREATED -> return next == RegionalCameraEventRecordStatus.VALIDATED || next == RegionalCameraEventRecordStatus.RETIRED
            RegionalCameraEventRecordStatus.VALIDATED -> return next == RegionalCameraEventRecordStatus.PUBLISHED
            RegionalCameraEventRecordStatus.PUBLISHED -> return next == RegionalCameraEventRecordStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalCameraEventRecordStatus) {
        if (!canMoveTo(next)) {
            error("regional camera event record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((createdAt.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): RegionalCameraEventRecord {
        return RegionalCameraEventRecord(
            code = code,
            title = title,
            margin = margin,
            quantity = quantity,
            attributes = attributes,
            sequence = sequence,
            stopCount = stopCount,
            createdAt = createdAt,
            discount = discount,
            budget = budget,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional camera event record in status created. */
fun newRegionalCameraEventRecord(code: String, title: String): RegionalCameraEventRecord {
    return RegionalCameraEventRecord(
        code = code,
        title = title,
        margin = 0.0,
        quantity = 0,
        attributes = mutableMapOf<String, String>(),
        sequence = 0,
        stopCount = 0,
        createdAt = 0,
        discount = 0.0,
        budget = 0.0,
        batchSize = 0,
        status = RegionalCameraEventRecordStatus.CREATED,
    )
}
