package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal fuel surcharge batch is in its life cycle. */
enum class SeasonalFuelSurchargeBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal fuel surcharge batch as the fuel surcharge team keeps it. */
data class SeasonalFuelSurchargeBatch(
    var code: String,
    var name: String,
    var score: Double,
    var endsAt: Int,
    var capacity: Int,
    var expiresAt: Int,
    var startsAt: Int,
    var label: String,
    var approved: Boolean,
    var summary: String,
    var status: SeasonalFuelSurchargeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalFuelSurchargeBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalFuelSurchargeBatchStatus): Boolean {
        when (status) {
            SeasonalFuelSurchargeBatchStatus.REQUESTED -> return next == SeasonalFuelSurchargeBatchStatus.REVOKED
            SeasonalFuelSurchargeBatchStatus.GRANTED -> return next == SeasonalFuelSurchargeBatchStatus.REVOKED || next == SeasonalFuelSurchargeBatchStatus.EXPIRED
            SeasonalFuelSurchargeBatchStatus.REVOKED -> return next == SeasonalFuelSurchargeBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalFuelSurchargeBatchStatus) {
        if (!canMoveTo(next)) {
            error("seasonal fuel surcharge batch $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"seasonal fuel surcharge batch"} $code"

    fun weightedValue(): Double = round((capacity.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalFuelSurchargeBatch {
        return SeasonalFuelSurchargeBatch(
            code = code,
            name = name,
            score = score,
            endsAt = endsAt,
            capacity = capacity,
            expiresAt = expiresAt,
            startsAt = startsAt,
            label = label,
            approved = approved,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal fuel surcharge batch in status requested. */
fun newSeasonalFuelSurchargeBatch(code: String, name: String): SeasonalFuelSurchargeBatch {
    return SeasonalFuelSurchargeBatch(
        code = code,
        name = name,
        score = 0.0,
        endsAt = 0,
        capacity = 0,
        expiresAt = 0,
        startsAt = 0,
        label = "",
        approved = false,
        summary = "",
        status = SeasonalFuelSurchargeBatchStatus.REQUESTED,
    )
}
