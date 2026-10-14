package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation adjustment is in its life cycle. */
enum class SortationAdjustmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A sortation adjustment as the sortation team keeps it. */
data class SortationAdjustment(
    var code: String,
    var label: String,
    var endsAt: Int,
    var channel: String,
    var visible: Boolean,
    var cost: Double,
    var checksum: String,
    var externalRef: String,
    var score: Double,
    var ownerId: String,
    var ratio: Double,
    var status: SortationAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationAdjustmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationAdjustmentStatus): Boolean {
        when (status) {
            SortationAdjustmentStatus.DRAFT -> {
                when (next) {
                    SortationAdjustmentStatus.PENDING, SortationAdjustmentStatus.ACTIVE -> return true
                }
                return false
            }
            SortationAdjustmentStatus.PENDING -> {
                when (next) {
                    SortationAdjustmentStatus.ACTIVE, SortationAdjustmentStatus.SUSPENDED -> return true
                }
                return false
            }
            SortationAdjustmentStatus.ACTIVE -> {
                when (next) {
                    SortationAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
            SortationAdjustmentStatus.SUSPENDED -> return next == SortationAdjustmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SortationAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("sortation adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round((cost - score) * 100) / 100

    fun snapshot(): SortationAdjustment {
        return SortationAdjustment(
            code = code,
            label = label,
            endsAt = endsAt,
            channel = channel,
            visible = visible,
            cost = cost,
            checksum = checksum,
            externalRef = externalRef,
            score = score,
            ownerId = ownerId,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a sortation adjustment in status draft. */
fun newSortationAdjustment(code: String, label: String): SortationAdjustment {
    return SortationAdjustment(
        code = code,
        label = label,
        endsAt = 0,
        channel = "",
        visible = false,
        cost = 0.0,
        checksum = "",
        externalRef = "",
        score = 0.0,
        ownerId = "",
        ratio = 0.0,
        status = SortationAdjustmentStatus.DRAFT,
    )
}
