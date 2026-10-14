package com.acme.transport.customs.declaration.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration tier is in its life cycle. */
enum class DeclarationTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A declaration tier as the declaration team keeps it. */
data class DeclarationTier(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var reviewer: String,
    var batchSize: Int,
    var driverId: String,
    var ceiling: Double,
    var checksum: String,
    var status: DeclarationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationTierStatus): Boolean {
        when (status) {
            DeclarationTierStatus.NEW -> {
                when (next) {
                    DeclarationTierStatus.RUNNING, DeclarationTierStatus.FAILED -> return true
                }
                return false
            }
            DeclarationTierStatus.QUEUED -> {
                when (next) {
                    DeclarationTierStatus.DONE, DeclarationTierStatus.FAILED -> return true
                }
                return false
            }
            DeclarationTierStatus.RUNNING -> {
                when (next) {
                    DeclarationTierStatus.DONE, DeclarationTierStatus.FAILED -> return true
                }
                return false
            }
            DeclarationTierStatus.DONE -> return next == DeclarationTierStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DeclarationTierStatus) {
        if (!canMoveTo(next)) {
            error("declaration tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round((batchSize.toDouble() + ceiling) * 100) / 100

    fun snapshot(): DeclarationTier {
        return DeclarationTier(
            code = code,
            title = title,
            pinned = pinned,
            reviewer = reviewer,
            batchSize = batchSize,
            driverId = driverId,
            ceiling = ceiling,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration tier in status new. */
fun newDeclarationTier(code: String, title: String): DeclarationTier {
    return DeclarationTier(
        code = code,
        title = title,
        pinned = false,
        reviewer = "",
        batchSize = 0,
        driverId = "",
        ceiling = 0.0,
        checksum = "",
        status = DeclarationTierStatus.NEW,
    )
}
