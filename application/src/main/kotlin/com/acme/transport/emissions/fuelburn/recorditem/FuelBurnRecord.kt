package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel burn record records of emissions's fuel burn work, and the rules, stores, and calculations around
// them.

/** Where a fuel burn record is in its life cycle. */
enum class FuelBurnRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fuel burn record. Its code is unique within a tenant. */
data class FuelBurnRecord(
    var code: String,
    var title: String,
    var version: Int,
    var attributes: MutableMap<String, String>,
    var maximum: Double,
    var sequence: Int,
    var retries: Int,
    var etaMinutes: Int,
    var archived: Boolean,
    var timezone: String,
    var description: String,
    var status: FuelBurnRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnRecordStatus): Boolean {
        when (status) {
            FuelBurnRecordStatus.CREATED -> {
                when (next) {
                    FuelBurnRecordStatus.RETIRED -> return true
                }
                return false
            }
            FuelBurnRecordStatus.VALIDATED -> {
                when (next) {
                    FuelBurnRecordStatus.RETIRED -> return true
                }
                return false
            }
            FuelBurnRecordStatus.PUBLISHED -> {
                when (next) {
                    FuelBurnRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnRecordStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn record $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((etaMinutes.toDouble() + maximum) * 100) / 100

    fun snapshot(): FuelBurnRecord {
        return FuelBurnRecord(
            code = code,
            title = title,
            version = version,
            attributes = attributes,
            maximum = maximum,
            sequence = sequence,
            retries = retries,
            etaMinutes = etaMinutes,
            archived = archived,
            timezone = timezone,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel burn record in status created. */
fun newFuelBurnRecord(code: String, title: String): FuelBurnRecord {
    return FuelBurnRecord(
        code = code,
        title = title,
        version = 0,
        attributes = mutableMapOf<String, String>(),
        maximum = 0.0,
        sequence = 0,
        retries = 0,
        etaMinutes = 0,
        archived = false,
        timezone = "",
        description = "",
        status = FuelBurnRecordStatus.CREATED,
    )
}
