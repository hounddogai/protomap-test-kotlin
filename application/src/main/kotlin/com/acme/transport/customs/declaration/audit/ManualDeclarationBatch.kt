package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual declaration batch is in its life cycle. */
enum class ManualDeclarationBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual declaration batch. Its code is unique within a tenant. */
data class ManualDeclarationBatch(
    var code: String,
    var name: String,
    var channel: String,
    var minimum: Double,
    var updatedAt: Int,
    var category: String,
    var fuelLiters: Double,
    var etaMinutes: Int,
    var sequence: Int,
    var status: ManualDeclarationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDeclarationBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDeclarationBatchStatus): Boolean {
        when (status) {
            ManualDeclarationBatchStatus.REQUESTED -> {
                when (next) {
                    ManualDeclarationBatchStatus.EXPIRED -> return true
                }
                return false
            }
            ManualDeclarationBatchStatus.GRANTED -> return next == ManualDeclarationBatchStatus.EXPIRED
            ManualDeclarationBatchStatus.REVOKED -> return next == ManualDeclarationBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ManualDeclarationBatchStatus) {
        if (!canMoveTo(next)) {
            error("manual declaration batch $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netValue(): Double = round((fuelLiters - minimum) * 100) / 100

    fun snapshot(): ManualDeclarationBatch {
        return ManualDeclarationBatch(
            code = code,
            name = name,
            channel = channel,
            minimum = minimum,
            updatedAt = updatedAt,
            category = category,
            fuelLiters = fuelLiters,
            etaMinutes = etaMinutes,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a manual declaration batch in status requested. */
fun newManualDeclarationBatch(code: String, name: String): ManualDeclarationBatch {
    return ManualDeclarationBatch(
        code = code,
        name = name,
        channel = "",
        minimum = 0.0,
        updatedAt = 0,
        category = "",
        fuelLiters = 0.0,
        etaMinutes = 0,
        sequence = 0,
        status = ManualDeclarationBatchStatus.REQUESTED,
    )
}
