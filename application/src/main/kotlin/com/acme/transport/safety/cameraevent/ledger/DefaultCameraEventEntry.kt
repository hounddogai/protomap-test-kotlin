package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default camera event entry is in its life cycle. */
enum class DefaultCameraEventEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One default camera event entry, with what the planners need to act on it. */
data class DefaultCameraEventEntry(
    var code: String,
    var label: String,
    var ceiling: Double,
    var ratio: Double,
    var expiresAt: Int,
    var visible: Boolean,
    var checksum: String,
    var phone: String,
    var floorValue: Double,
    var discount: Double,
    var status: DefaultCameraEventEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCameraEventEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCameraEventEntryStatus): Boolean {
        when (status) {
            DefaultCameraEventEntryStatus.NEW -> return next == DefaultCameraEventEntryStatus.FAILED
            DefaultCameraEventEntryStatus.QUEUED -> return next == DefaultCameraEventEntryStatus.RUNNING || next == DefaultCameraEventEntryStatus.DONE
            DefaultCameraEventEntryStatus.RUNNING -> return next == DefaultCameraEventEntryStatus.DONE || next == DefaultCameraEventEntryStatus.FAILED
            DefaultCameraEventEntryStatus.DONE -> return next == DefaultCameraEventEntryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultCameraEventEntryStatus) {
        if (!canMoveTo(next)) {
            error("default camera event entry $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round((expiresAt.toDouble() + discount) * 100) / 100

    fun snapshot(): DefaultCameraEventEntry {
        return DefaultCameraEventEntry(
            code = code,
            label = label,
            ceiling = ceiling,
            ratio = ratio,
            expiresAt = expiresAt,
            visible = visible,
            checksum = checksum,
            phone = phone,
            floorValue = floorValue,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default camera event entry in status new. */
fun newDefaultCameraEventEntry(code: String, label: String): DefaultCameraEventEntry {
    return DefaultCameraEventEntry(
        code = code,
        label = label,
        ceiling = 0.0,
        ratio = 0.0,
        expiresAt = 0,
        visible = false,
        checksum = "",
        phone = "",
        floorValue = 0.0,
        discount = 0.0,
        status = DefaultCameraEventEntryStatus.NEW,
    )
}
