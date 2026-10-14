package com.acme.transport.emissions.idling.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default idling entry is in its life cycle. */
enum class DefaultIdlingEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a default idling entry and its current status. */
data class DefaultIdlingEntry(
    var code: String,
    var name: String,
    var checksum: String,
    var pallets: Int,
    var ratio: Double,
    var approved: Boolean,
    var etaMinutes: Int,
    var source: String,
    var revision: Int,
    var reviewer: String,
    var discount: Double,
    var status: DefaultIdlingEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultIdlingEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultIdlingEntryStatus): Boolean {
        when (status) {
            DefaultIdlingEntryStatus.CREATED -> {
                when (next) {
                    DefaultIdlingEntryStatus.VALIDATED -> return true
                }
                return false
            }
            DefaultIdlingEntryStatus.VALIDATED -> {
                when (next) {
                    DefaultIdlingEntryStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultIdlingEntryStatus.PUBLISHED -> {
                when (next) {
                    DefaultIdlingEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultIdlingEntryStatus) {
        if (!canMoveTo(next)) {
            error("default idling entry $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun adjustedExposure(): Double = round((pallets.toDouble() - ratio) * 100) / 100

    fun snapshot(): DefaultIdlingEntry {
        return DefaultIdlingEntry(
            code = code,
            name = name,
            checksum = checksum,
            pallets = pallets,
            ratio = ratio,
            approved = approved,
            etaMinutes = etaMinutes,
            source = source,
            revision = revision,
            reviewer = reviewer,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default idling entry in status created. */
fun newDefaultIdlingEntry(code: String, name: String): DefaultIdlingEntry {
    return DefaultIdlingEntry(
        code = code,
        name = name,
        checksum = "",
        pallets = 0,
        ratio = 0.0,
        approved = false,
        etaMinutes = 0,
        source = "",
        revision = 0,
        reviewer = "",
        discount = 0.0,
        status = DefaultIdlingEntryStatus.CREATED,
    )
}
