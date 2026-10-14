package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation reservation is in its life cycle. */
enum class SortationReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A sortation reservation. Its code is unique within a tenant. */
data class SortationReservation(
    var code: String,
    var title: String,
    var dock: String,
    var createdAt: Int,
    var channel: String,
    var quantity: Int,
    var updatedAt: Int,
    var checksum: String,
    var approved: Boolean,
    var ownerId: String,
    var locale: String,
    var status: SortationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationReservationStatus): Boolean {
        when (status) {
            SortationReservationStatus.NEW -> {
                when (next) {
                    SortationReservationStatus.QUEUED, SortationReservationStatus.FAILED -> return true
                }
                return false
            }
            SortationReservationStatus.QUEUED -> return next == SortationReservationStatus.DONE
            SortationReservationStatus.RUNNING -> {
                when (next) {
                    SortationReservationStatus.DONE, SortationReservationStatus.FAILED -> return true
                }
                return false
            }
            SortationReservationStatus.DONE -> {
                when (next) {
                    SortationReservationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationReservationStatus) {
        if (!canMoveTo(next)) {
            error("sortation reservation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"sortation reservation"} $code"

    fun netTotal(): Double = round((createdAt.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): SortationReservation {
        return SortationReservation(
            code = code,
            title = title,
            dock = dock,
            createdAt = createdAt,
            channel = channel,
            quantity = quantity,
            updatedAt = updatedAt,
            checksum = checksum,
            approved = approved,
            ownerId = ownerId,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a sortation reservation in status new. */
fun newSortationReservation(code: String, title: String): SortationReservation {
    return SortationReservation(
        code = code,
        title = title,
        dock = "",
        createdAt = 0,
        channel = "",
        quantity = 0,
        updatedAt = 0,
        checksum = "",
        approved = false,
        ownerId = "",
        locale = "",
        status = SortationReservationStatus.NEW,
    )
}
