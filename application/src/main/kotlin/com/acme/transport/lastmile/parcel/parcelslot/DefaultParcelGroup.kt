package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default parcel group is in its life cycle. */
enum class DefaultParcelGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default parcel group. Its code is unique within a tenant. */
data class DefaultParcelGroup(
    var code: String,
    var label: String,
    var region: String,
    var margin: Double,
    var quantity: Int,
    var status: DefaultParcelGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultParcelGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultParcelGroupStatus): Boolean {
        when (status) {
            DefaultParcelGroupStatus.DRAFT -> {
                when (next) {
                    DefaultParcelGroupStatus.ACTIVE, DefaultParcelGroupStatus.CLOSED -> return true
                }
                return false
            }
            DefaultParcelGroupStatus.PENDING -> {
                when (next) {
                    DefaultParcelGroupStatus.ACTIVE, DefaultParcelGroupStatus.CLOSED -> return true
                }
                return false
            }
            DefaultParcelGroupStatus.ACTIVE -> return next == DefaultParcelGroupStatus.CLOSED
            DefaultParcelGroupStatus.SUSPENDED -> {
                when (next) {
                    DefaultParcelGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultParcelGroupStatus) {
        if (!canMoveTo(next)) {
            error("default parcel group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netValue(): Double = round(margin * quantity.toDouble() * 100) / 100

    fun snapshot(): DefaultParcelGroup {
        return DefaultParcelGroup(
            code = code,
            label = label,
            region = region,
            margin = margin,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default parcel group in status draft. */
fun newDefaultParcelGroup(code: String, label: String): DefaultParcelGroup {
    return DefaultParcelGroup(
        code = code,
        label = label,
        region = "",
        margin = 0.0,
        quantity = 0,
        status = DefaultParcelGroupStatus.DRAFT,
    )
}
