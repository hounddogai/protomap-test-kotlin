package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door batch is in its life cycle. */
enum class DockDoorBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A dock door batch. Its code is unique within a tenant. */
data class DockDoorBatch(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var quantity: Int,
    var limit: Int,
    var ratio: Double,
    var retries: Int,
    var status: DockDoorBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorBatchStatus): Boolean {
        when (status) {
            DockDoorBatchStatus.CREATED -> return next == DockDoorBatchStatus.RETIRED
            DockDoorBatchStatus.VALIDATED -> return next == DockDoorBatchStatus.PUBLISHED
            DockDoorBatchStatus.PUBLISHED -> return next == DockDoorBatchStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DockDoorBatchStatus) {
        if (!canMoveTo(next)) {
            error("dock door batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedValue(): Double = round((limit.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): DockDoorBatch {
        return DockDoorBatch(
            code = code,
            title = title,
            distanceKm = distanceKm,
            quantity = quantity,
            limit = limit,
            ratio = ratio,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dock door batch in status created. */
fun newDockDoorBatch(code: String, title: String): DockDoorBatch {
    return DockDoorBatch(
        code = code,
        title = title,
        distanceKm = 0.0,
        quantity = 0,
        limit = 0,
        ratio = 0.0,
        retries = 0,
        status = DockDoorBatchStatus.CREATED,
    )
}
