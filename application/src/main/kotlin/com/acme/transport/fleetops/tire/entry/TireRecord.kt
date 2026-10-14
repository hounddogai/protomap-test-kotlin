package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire record is in its life cycle. */
enum class TireRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tire record. Its code is unique within a tenant. */
data class TireRecord(
    var code: String,
    var label: String,
    var title: String,
    var capacity: Int,
    var contactEmail: String,
    var fuelLiters: Double,
    var status: TireRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireRecordStatus): Boolean {
        when (status) {
            TireRecordStatus.PLANNED -> {
                when (next) {
                    TireRecordStatus.SCHEDULED -> return true
                }
                return false
            }
            TireRecordStatus.SCHEDULED -> return next == TireRecordStatus.IN_PROGRESS || next == TireRecordStatus.COMPLETED
            TireRecordStatus.IN_PROGRESS -> {
                when (next) {
                    TireRecordStatus.COMPLETED, TireRecordStatus.CANCELLED -> return true
                }
                return false
            }
            TireRecordStatus.COMPLETED -> {
                when (next) {
                    TireRecordStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireRecordStatus) {
        if (!canMoveTo(next)) {
            error("tire record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tire record"} $code"

    fun netTotal(): Double = round(capacity.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): TireRecord {
        return TireRecord(
            code = code,
            label = label,
            title = title,
            capacity = capacity,
            contactEmail = contactEmail,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tire record in status planned. */
fun newTireRecord(code: String, label: String): TireRecord {
    return TireRecord(
        code = code,
        label = label,
        title = "",
        capacity = 0,
        contactEmail = "",
        fuelLiters = 0.0,
        status = TireRecordStatus.PLANNED,
    )
}
