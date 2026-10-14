package com.acme.transport.customs.declaration.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending declaration variance records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a pending declaration variance is in its life cycle. */
enum class PendingDeclarationVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending declaration variance. Its code is unique within a tenant. */
data class PendingDeclarationVariance(
    var code: String,
    var label: String,
    var threshold: Double,
    var archived: Boolean,
    var pallets: Int,
    var phone: String,
    var routeCode: String,
    var status: PendingDeclarationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDeclarationVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDeclarationVarianceStatus): Boolean {
        when (status) {
            PendingDeclarationVarianceStatus.REQUESTED -> return next == PendingDeclarationVarianceStatus.EXPIRED
            PendingDeclarationVarianceStatus.GRANTED -> return next == PendingDeclarationVarianceStatus.REVOKED
            PendingDeclarationVarianceStatus.REVOKED -> {
                when (next) {
                    PendingDeclarationVarianceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDeclarationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("pending declaration variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedLoad(): Double = round((pallets.toDouble() - threshold) * 100) / 100

    fun snapshot(): PendingDeclarationVariance {
        return PendingDeclarationVariance(
            code = code,
            label = label,
            threshold = threshold,
            archived = archived,
            pallets = pallets,
            phone = phone,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending declaration variance in status requested. */
fun newPendingDeclarationVariance(code: String, label: String): PendingDeclarationVariance {
    return PendingDeclarationVariance(
        code = code,
        label = label,
        threshold = 0.0,
        archived = false,
        pallets = 0,
        phone = "",
        routeCode = "",
        status = PendingDeclarationVarianceStatus.REQUESTED,
    )
}
