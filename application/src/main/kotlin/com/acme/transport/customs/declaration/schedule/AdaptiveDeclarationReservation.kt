package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive declaration reservation is in its life cycle. */
enum class AdaptiveDeclarationReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A adaptive declaration reservation. Its code is unique within a tenant. */
data class AdaptiveDeclarationReservation(
    var code: String,
    var name: String,
    var ownerId: String,
    var quantity: Int,
    var region: String,
    var pinned: Boolean,
    var capacity: Int,
    var country: String,
    var visible: Boolean,
    var minimum: Double,
    var status: AdaptiveDeclarationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDeclarationReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDeclarationReservationStatus): Boolean {
        when (status) {
            AdaptiveDeclarationReservationStatus.OPEN -> return next == AdaptiveDeclarationReservationStatus.ARCHIVED
            AdaptiveDeclarationReservationStatus.IN_REVIEW -> return next == AdaptiveDeclarationReservationStatus.RESOLVED
            AdaptiveDeclarationReservationStatus.RESOLVED -> return next == AdaptiveDeclarationReservationStatus.ARCHIVED
            AdaptiveDeclarationReservationStatus.REOPENED -> return next == AdaptiveDeclarationReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AdaptiveDeclarationReservationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive declaration reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round((capacity.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveDeclarationReservation {
        return AdaptiveDeclarationReservation(
            code = code,
            name = name,
            ownerId = ownerId,
            quantity = quantity,
            region = region,
            pinned = pinned,
            capacity = capacity,
            country = country,
            visible = visible,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive declaration reservation in status open. */
fun newAdaptiveDeclarationReservation(code: String, name: String): AdaptiveDeclarationReservation {
    return AdaptiveDeclarationReservation(
        code = code,
        name = name,
        ownerId = "",
        quantity = 0,
        region = "",
        pinned = false,
        capacity = 0,
        country = "",
        visible = false,
        minimum = 0.0,
        status = AdaptiveDeclarationReservationStatus.OPEN,
    )
}
