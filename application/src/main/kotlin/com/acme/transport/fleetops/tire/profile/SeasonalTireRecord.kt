package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal tire record is in its life cycle. */
enum class SeasonalTireRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal tire record. Its code is unique within a tenant. */
data class SeasonalTireRecord(
    var code: String,
    var label: String,
    var dock: String,
    var routeCode: String,
    var notes: String,
    var batchSize: Int,
    var category: String,
    var lane: String,
    var discount: Double,
    var country: String,
    var status: SeasonalTireRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTireRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTireRecordStatus): Boolean {
        when (status) {
            SeasonalTireRecordStatus.NEW -> {
                when (next) {
                    SeasonalTireRecordStatus.RUNNING, SeasonalTireRecordStatus.FAILED -> return true
                }
                return false
            }
            SeasonalTireRecordStatus.QUEUED -> return next == SeasonalTireRecordStatus.RUNNING
            SeasonalTireRecordStatus.RUNNING -> return next == SeasonalTireRecordStatus.DONE || next == SeasonalTireRecordStatus.FAILED
            SeasonalTireRecordStatus.DONE -> return next == SeasonalTireRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalTireRecordStatus) {
        if (!canMoveTo(next)) {
            error("seasonal tire record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round((discount - batchSize.toDouble()) * 100) / 100

    fun snapshot(): SeasonalTireRecord {
        return SeasonalTireRecord(
            code = code,
            label = label,
            dock = dock,
            routeCode = routeCode,
            notes = notes,
            batchSize = batchSize,
            category = category,
            lane = lane,
            discount = discount,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal tire record in status new. */
fun newSeasonalTireRecord(code: String, label: String): SeasonalTireRecord {
    return SeasonalTireRecord(
        code = code,
        label = label,
        dock = "",
        routeCode = "",
        notes = "",
        batchSize = 0,
        category = "",
        lane = "",
        discount = 0.0,
        country = "",
        status = SeasonalTireRecordStatus.NEW,
    )
}
