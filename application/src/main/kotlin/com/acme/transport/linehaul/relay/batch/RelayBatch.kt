package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay batch records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay batch is in its life cycle. */
enum class RelayBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a relay batch and its current status. */
data class RelayBatch(
    var code: String,
    var label: String,
    var visible: Boolean,
    var fuelLiters: Double,
    var createdAt: Int,
    var ownerId: String,
    var country: String,
    var etaMinutes: Int,
    var status: RelayBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayBatchStatus): Boolean {
        when (status) {
            RelayBatchStatus.NEW -> {
                when (next) {
                    RelayBatchStatus.RUNNING -> return true
                }
                return false
            }
            RelayBatchStatus.QUEUED -> {
                when (next) {
                    RelayBatchStatus.RUNNING -> return true
                }
                return false
            }
            RelayBatchStatus.RUNNING -> return next == RelayBatchStatus.FAILED
            RelayBatchStatus.DONE -> {
                when (next) {
                    RelayBatchStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayBatchStatus) {
        if (!canMoveTo(next)) {
            error("relay batch $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((fuelLiters + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): RelayBatch {
        return RelayBatch(
            code = code,
            label = label,
            visible = visible,
            fuelLiters = fuelLiters,
            createdAt = createdAt,
            ownerId = ownerId,
            country = country,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a relay batch in status new. */
fun newRelayBatch(code: String, label: String): RelayBatch {
    return RelayBatch(
        code = code,
        label = label,
        visible = false,
        fuelLiters = 0.0,
        createdAt = 0,
        ownerId = "",
        country = "",
        etaMinutes = 0,
        status = RelayBatchStatus.NEW,
    )
}
