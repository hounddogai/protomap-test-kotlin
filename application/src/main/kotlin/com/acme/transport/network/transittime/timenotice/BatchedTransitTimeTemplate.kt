package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched transit time template is in its life cycle. */
enum class BatchedTransitTimeTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a batched transit time template and its current status. */
data class BatchedTransitTimeTemplate(
    var code: String,
    var name: String,
    var version: Int,
    var summary: String,
    var ratio: Double,
    var timezone: String,
    var batchSize: Int,
    var contactEmail: String,
    var description: String,
    var odometer: Int,
    var pinned: Boolean,
    var status: BatchedTransitTimeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTransitTimeTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTransitTimeTemplateStatus): Boolean {
        when (status) {
            BatchedTransitTimeTemplateStatus.IDLE -> {
                when (next) {
                    BatchedTransitTimeTemplateStatus.STOPPED -> return true
                }
                return false
            }
            BatchedTransitTimeTemplateStatus.WARMING -> {
                when (next) {
                    BatchedTransitTimeTemplateStatus.READY, BatchedTransitTimeTemplateStatus.DRAINING -> return true
                }
                return false
            }
            BatchedTransitTimeTemplateStatus.READY -> return next == BatchedTransitTimeTemplateStatus.STOPPED
            BatchedTransitTimeTemplateStatus.DRAINING -> {
                when (next) {
                    BatchedTransitTimeTemplateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTransitTimeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("batched transit time template $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun netVolume(): Double = round((batchSize.toDouble() - ratio) * 100) / 100

    fun snapshot(): BatchedTransitTimeTemplate {
        return BatchedTransitTimeTemplate(
            code = code,
            name = name,
            version = version,
            summary = summary,
            ratio = ratio,
            timezone = timezone,
            batchSize = batchSize,
            contactEmail = contactEmail,
            description = description,
            odometer = odometer,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched transit time template in status idle. */
fun newBatchedTransitTimeTemplate(code: String, name: String): BatchedTransitTimeTemplate {
    return BatchedTransitTimeTemplate(
        code = code,
        name = name,
        version = 0,
        summary = "",
        ratio = 0.0,
        timezone = "",
        batchSize = 0,
        contactEmail = "",
        description = "",
        odometer = 0,
        pinned = false,
        status = BatchedTransitTimeTemplateStatus.IDLE,
    )
}
