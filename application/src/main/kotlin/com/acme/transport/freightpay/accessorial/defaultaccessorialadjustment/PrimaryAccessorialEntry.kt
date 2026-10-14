package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary accessorial entry is in its life cycle. */
enum class PrimaryAccessorialEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary accessorial entry as the accessorial team keeps it. */
data class PrimaryAccessorialEntry(
    var code: String,
    var name: String,
    var version: Int,
    var region: String,
    var active: Boolean,
    var distanceKm: Double,
    var revision: Int,
    var checksum: String,
    var segment: String,
    var windowDays: Int,
    var rate: Double,
    var status: PrimaryAccessorialEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAccessorialEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAccessorialEntryStatus): Boolean {
        when (status) {
            PrimaryAccessorialEntryStatus.NEW -> {
                when (next) {
                    PrimaryAccessorialEntryStatus.RUNNING, PrimaryAccessorialEntryStatus.DONE -> return true
                }
                return false
            }
            PrimaryAccessorialEntryStatus.QUEUED -> return next == PrimaryAccessorialEntryStatus.RUNNING
            PrimaryAccessorialEntryStatus.RUNNING -> return next == PrimaryAccessorialEntryStatus.FAILED
            PrimaryAccessorialEntryStatus.DONE -> {
                when (next) {
                    PrimaryAccessorialEntryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryAccessorialEntryStatus) {
        if (!canMoveTo(next)) {
            error("primary accessorial entry $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun netValue(): Double = round((version.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): PrimaryAccessorialEntry {
        return PrimaryAccessorialEntry(
            code = code,
            name = name,
            version = version,
            region = region,
            active = active,
            distanceKm = distanceKm,
            revision = revision,
            checksum = checksum,
            segment = segment,
            windowDays = windowDays,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a primary accessorial entry in status new. */
fun newPrimaryAccessorialEntry(code: String, name: String): PrimaryAccessorialEntry {
    return PrimaryAccessorialEntry(
        code = code,
        name = name,
        version = 0,
        region = "",
        active = false,
        distanceKm = 0.0,
        revision = 0,
        checksum = "",
        segment = "",
        windowDays = 0,
        rate = 0.0,
        status = PrimaryAccessorialEntryStatus.NEW,
    )
}
