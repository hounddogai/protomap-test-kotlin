package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled fuel surcharge draft records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a scheduled fuel surcharge draft is in its life cycle. */
enum class ScheduledFuelSurchargeDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled fuel surcharge draft. Its code is unique within a tenant. */
data class ScheduledFuelSurchargeDraft(
    var code: String,
    var title: String,
    var trailerId: String,
    var endsAt: Int,
    var score: Double,
    var carrier: String,
    var checksum: String,
    var status: ScheduledFuelSurchargeDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFuelSurchargeDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFuelSurchargeDraftStatus): Boolean {
        when (status) {
            ScheduledFuelSurchargeDraftStatus.CREATED -> {
                when (next) {
                    ScheduledFuelSurchargeDraftStatus.PUBLISHED -> return true
                }
                return false
            }
            ScheduledFuelSurchargeDraftStatus.VALIDATED -> {
                when (next) {
                    ScheduledFuelSurchargeDraftStatus.PUBLISHED, ScheduledFuelSurchargeDraftStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledFuelSurchargeDraftStatus.PUBLISHED -> return next == ScheduledFuelSurchargeDraftStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledFuelSurchargeDraftStatus) {
        if (!canMoveTo(next)) {
            error("scheduled fuel surcharge draft $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((endsAt.toDouble() - score) * 100) / 100

    fun snapshot(): ScheduledFuelSurchargeDraft {
        return ScheduledFuelSurchargeDraft(
            code = code,
            title = title,
            trailerId = trailerId,
            endsAt = endsAt,
            score = score,
            carrier = carrier,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled fuel surcharge draft in status created. */
fun newScheduledFuelSurchargeDraft(code: String, title: String): ScheduledFuelSurchargeDraft {
    return ScheduledFuelSurchargeDraft(
        code = code,
        title = title,
        trailerId = "",
        endsAt = 0,
        score = 0.0,
        carrier = "",
        checksum = "",
        status = ScheduledFuelSurchargeDraftStatus.CREATED,
    )
}
