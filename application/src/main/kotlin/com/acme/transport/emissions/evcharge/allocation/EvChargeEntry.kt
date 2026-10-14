package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge entry is in its life cycle. */
enum class EvChargeEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A ev charge entry. Its code is unique within a tenant. */
data class EvChargeEntry(
    var code: String,
    var label: String,
    var cost: Double,
    var revision: Int,
    var maximum: Double,
    var archived: Boolean,
    var dock: String,
    var source: String,
    var windowDays: Int,
    var floorValue: Double,
    var ownerId: String,
    var status: EvChargeEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeEntryStatus): Boolean {
        when (status) {
            EvChargeEntryStatus.CREATED -> return next == EvChargeEntryStatus.PUBLISHED || next == EvChargeEntryStatus.RETIRED
            EvChargeEntryStatus.VALIDATED -> return next == EvChargeEntryStatus.PUBLISHED || next == EvChargeEntryStatus.RETIRED
            EvChargeEntryStatus.PUBLISHED -> return next == EvChargeEntryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: EvChargeEntryStatus) {
        if (!canMoveTo(next)) {
            error("ev charge entry $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round((maximum - cost) * 100) / 100

    fun snapshot(): EvChargeEntry {
        return EvChargeEntry(
            code = code,
            label = label,
            cost = cost,
            revision = revision,
            maximum = maximum,
            archived = archived,
            dock = dock,
            source = source,
            windowDays = windowDays,
            floorValue = floorValue,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a ev charge entry in status created. */
fun newEvChargeEntry(code: String, label: String): EvChargeEntry {
    return EvChargeEntry(
        code = code,
        label = label,
        cost = 0.0,
        revision = 0,
        maximum = 0.0,
        archived = false,
        dock = "",
        source = "",
        windowDays = 0,
        floorValue = 0.0,
        ownerId = "",
        status = EvChargeEntryStatus.CREATED,
    )
}
