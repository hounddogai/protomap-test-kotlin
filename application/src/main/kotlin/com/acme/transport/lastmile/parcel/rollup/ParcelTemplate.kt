package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel template is in its life cycle. */
enum class ParcelTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A parcel template as the parcel team keeps it. */
data class ParcelTemplate(
    var code: String,
    var name: String,
    var ceiling: Double,
    var pallets: Int,
    var sequence: Int,
    var status: ParcelTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelTemplateStatus): Boolean {
        when (status) {
            ParcelTemplateStatus.CREATED -> return next == ParcelTemplateStatus.VALIDATED
            ParcelTemplateStatus.VALIDATED -> {
                when (next) {
                    ParcelTemplateStatus.PUBLISHED, ParcelTemplateStatus.RETIRED -> return true
                }
                return false
            }
            ParcelTemplateStatus.PUBLISHED -> {
                when (next) {
                    ParcelTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelTemplateStatus) {
        if (!canMoveTo(next)) {
            error("parcel template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round(pallets.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): ParcelTemplate {
        return ParcelTemplate(
            code = code,
            name = name,
            ceiling = ceiling,
            pallets = pallets,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a parcel template in status created. */
fun newParcelTemplate(code: String, name: String): ParcelTemplate {
    return ParcelTemplate(
        code = code,
        name = name,
        ceiling = 0.0,
        pallets = 0,
        sequence = 0,
        status = ParcelTemplateStatus.CREATED,
    )
}
