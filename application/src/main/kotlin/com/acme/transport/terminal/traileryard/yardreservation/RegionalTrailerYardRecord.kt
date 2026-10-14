package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional trailer yard record is in its life cycle. */
enum class RegionalTrailerYardRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional trailer yard record. Its code is unique within a tenant. */
data class RegionalTrailerYardRecord(
    var code: String,
    var title: String,
    var floorValue: Double,
    var region: String,
    var etaMinutes: Int,
    var weight: Double,
    var ceiling: Double,
    var graceDays: Int,
    var ratio: Double,
    var label: String,
    var priority: Int,
    var status: RegionalTrailerYardRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerYardRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerYardRecordStatus): Boolean {
        when (status) {
            RegionalTrailerYardRecordStatus.CREATED -> {
                when (next) {
                    RegionalTrailerYardRecordStatus.RETIRED -> return true
                }
                return false
            }
            RegionalTrailerYardRecordStatus.VALIDATED -> return next == RegionalTrailerYardRecordStatus.PUBLISHED || next == RegionalTrailerYardRecordStatus.RETIRED
            RegionalTrailerYardRecordStatus.PUBLISHED -> {
                when (next) {
                    RegionalTrailerYardRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalTrailerYardRecordStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer yard record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun netValue(): Double = round((etaMinutes.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): RegionalTrailerYardRecord {
        return RegionalTrailerYardRecord(
            code = code,
            title = title,
            floorValue = floorValue,
            region = region,
            etaMinutes = etaMinutes,
            weight = weight,
            ceiling = ceiling,
            graceDays = graceDays,
            ratio = ratio,
            label = label,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a regional trailer yard record in status created. */
fun newRegionalTrailerYardRecord(code: String, title: String): RegionalTrailerYardRecord {
    return RegionalTrailerYardRecord(
        code = code,
        title = title,
        floorValue = 0.0,
        region = "",
        etaMinutes = 0,
        weight = 0.0,
        ceiling = 0.0,
        graceDays = 0,
        ratio = 0.0,
        label = "",
        priority = 0,
        status = RegionalTrailerYardRecordStatus.CREATED,
    )
}
