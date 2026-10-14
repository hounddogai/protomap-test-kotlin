package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary emission factor group is in its life cycle. */
enum class PrimaryEmissionFactorGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary emission factor group as the emission factor team keeps it. */
data class PrimaryEmissionFactorGroup(
    var code: String,
    var name: String,
    var amount: Double,
    var trailerId: String,
    var batchSize: Int,
    var checksum: String,
    var status: PrimaryEmissionFactorGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryEmissionFactorGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryEmissionFactorGroupStatus): Boolean {
        when (status) {
            PrimaryEmissionFactorGroupStatus.CREATED -> return next == PrimaryEmissionFactorGroupStatus.VALIDATED || next == PrimaryEmissionFactorGroupStatus.PUBLISHED
            PrimaryEmissionFactorGroupStatus.VALIDATED -> {
                when (next) {
                    PrimaryEmissionFactorGroupStatus.PUBLISHED, PrimaryEmissionFactorGroupStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryEmissionFactorGroupStatus.PUBLISHED -> return next == PrimaryEmissionFactorGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryEmissionFactorGroupStatus) {
        if (!canMoveTo(next)) {
            error("primary emission factor group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((amount - batchSize.toDouble()) * 100) / 100

    fun snapshot(): PrimaryEmissionFactorGroup {
        return PrimaryEmissionFactorGroup(
            code = code,
            name = name,
            amount = amount,
            trailerId = trailerId,
            batchSize = batchSize,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary emission factor group in status created. */
fun newPrimaryEmissionFactorGroup(code: String, name: String): PrimaryEmissionFactorGroup {
    return PrimaryEmissionFactorGroup(
        code = code,
        name = name,
        amount = 0.0,
        trailerId = "",
        batchSize = 0,
        checksum = "",
        status = PrimaryEmissionFactorGroupStatus.CREATED,
    )
}
