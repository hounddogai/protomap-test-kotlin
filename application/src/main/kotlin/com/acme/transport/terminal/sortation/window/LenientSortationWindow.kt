package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient sortation window is in its life cycle. */
enum class LenientSortationWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient sortation window as the sortation team keeps it. */
data class LenientSortationWindow(
    var code: String,
    var name: String,
    var notes: String,
    var sequence: Int,
    var ownerId: String,
    var batchSize: Int,
    var status: LenientSortationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientSortationWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientSortationWindowStatus): Boolean {
        when (status) {
            LenientSortationWindowStatus.NEW -> return next == LenientSortationWindowStatus.RUNNING
            LenientSortationWindowStatus.QUEUED -> return next == LenientSortationWindowStatus.DONE || next == LenientSortationWindowStatus.FAILED
            LenientSortationWindowStatus.RUNNING -> {
                when (next) {
                    LenientSortationWindowStatus.DONE, LenientSortationWindowStatus.FAILED -> return true
                }
                return false
            }
            LenientSortationWindowStatus.DONE -> {
                when (next) {
                    LenientSortationWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientSortationWindowStatus) {
        if (!canMoveTo(next)) {
            error("lenient sortation window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient sortation window"} $code"

    fun projectedTotal(): Double = round((sequence.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): LenientSortationWindow {
        return LenientSortationWindow(
            code = code,
            name = name,
            notes = notes,
            sequence = sequence,
            ownerId = ownerId,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient sortation window in status new. */
fun newLenientSortationWindow(code: String, name: String): LenientSortationWindow {
    return LenientSortationWindow(
        code = code,
        name = name,
        notes = "",
        sequence = 0,
        ownerId = "",
        batchSize = 0,
        status = LenientSortationWindowStatus.NEW,
    )
}
