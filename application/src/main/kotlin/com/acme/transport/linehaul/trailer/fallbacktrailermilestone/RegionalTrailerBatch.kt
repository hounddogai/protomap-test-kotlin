package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.kit.text.normalize

/** Where a regional trailer batch is in its life cycle. */
enum class RegionalTrailerBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional trailer batch. Its code is unique within a tenant. */
data class RegionalTrailerBatch(
    var code: String,
    var title: String,
    var region: String,
    var unit: String,
    var trailerId: String,
    var channel: String,
    var checksum: String,
    var contactEmail: String,
    var country: String,
    var active: Boolean,
    var quantity: Int,
    var status: RegionalTrailerBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerBatchStatus): Boolean {
        when (status) {
            RegionalTrailerBatchStatus.DRAFT -> {
                when (next) {
                    RegionalTrailerBatchStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalTrailerBatchStatus.PENDING -> {
                when (next) {
                    RegionalTrailerBatchStatus.SUSPENDED, RegionalTrailerBatchStatus.CLOSED -> return true
                }
                return false
            }
            RegionalTrailerBatchStatus.ACTIVE -> return next == RegionalTrailerBatchStatus.SUSPENDED || next == RegionalTrailerBatchStatus.CLOSED
            RegionalTrailerBatchStatus.SUSPENDED -> {
                when (next) {
                    RegionalTrailerBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalTrailerBatchStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional trailer batch"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): RegionalTrailerBatch {
        return RegionalTrailerBatch(
            code = code,
            title = title,
            region = region,
            unit = unit,
            trailerId = trailerId,
            channel = channel,
            checksum = checksum,
            contactEmail = contactEmail,
            country = country,
            active = active,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a regional trailer batch in status draft. */
fun newRegionalTrailerBatch(code: String, title: String): RegionalTrailerBatch {
    return RegionalTrailerBatch(
        code = code,
        title = title,
        region = "",
        unit = "",
        trailerId = "",
        channel = "",
        checksum = "",
        contactEmail = "",
        country = "",
        active = false,
        quantity = 0,
        status = RegionalTrailerBatchStatus.DRAFT,
    )
}
