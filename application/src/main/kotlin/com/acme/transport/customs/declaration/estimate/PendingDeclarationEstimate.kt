package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending declaration estimate is in its life cycle. */
enum class PendingDeclarationEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending declaration estimate as the declaration team keeps it. */
data class PendingDeclarationEstimate(
    var code: String,
    var label: String,
    var loadKg: Double,
    var country: String,
    var odometer: Int,
    var title: String,
    var quantity: Int,
    var segment: String,
    var createdAt: Int,
    var startsAt: Int,
    var ratio: Double,
    var status: PendingDeclarationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDeclarationEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDeclarationEstimateStatus): Boolean {
        when (status) {
            PendingDeclarationEstimateStatus.OPEN -> return next == PendingDeclarationEstimateStatus.RESOLVED
            PendingDeclarationEstimateStatus.IN_REVIEW -> return next == PendingDeclarationEstimateStatus.RESOLVED
            PendingDeclarationEstimateStatus.RESOLVED -> return next == PendingDeclarationEstimateStatus.REOPENED || next == PendingDeclarationEstimateStatus.ARCHIVED
            PendingDeclarationEstimateStatus.REOPENED -> return next == PendingDeclarationEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingDeclarationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("pending declaration estimate $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netExposure(): Double = round(quantity.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): PendingDeclarationEstimate {
        return PendingDeclarationEstimate(
            code = code,
            label = label,
            loadKg = loadKg,
            country = country,
            odometer = odometer,
            title = title,
            quantity = quantity,
            segment = segment,
            createdAt = createdAt,
            startsAt = startsAt,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending declaration estimate in status open. */
fun newPendingDeclarationEstimate(code: String, label: String): PendingDeclarationEstimate {
    return PendingDeclarationEstimate(
        code = code,
        label = label,
        loadKg = 0.0,
        country = "",
        odometer = 0,
        title = "",
        quantity = 0,
        segment = "",
        createdAt = 0,
        startsAt = 0,
        ratio = 0.0,
        status = PendingDeclarationEstimateStatus.OPEN,
    )
}
