package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient parcel mapping is in its life cycle. */
enum class LenientParcelMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient parcel mapping as the parcel team keeps it. */
data class LenientParcelMapping(
    var code: String,
    var name: String,
    var description: String,
    var amount: Double,
    var weight: Double,
    var loadKg: Double,
    var title: String,
    var currency: String,
    var checksum: String,
    var category: String,
    var status: LenientParcelMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientParcelMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientParcelMappingStatus): Boolean {
        when (status) {
            LenientParcelMappingStatus.OPEN -> return next == LenientParcelMappingStatus.RESOLVED
            LenientParcelMappingStatus.IN_REVIEW -> return next == LenientParcelMappingStatus.REOPENED
            LenientParcelMappingStatus.RESOLVED -> return next == LenientParcelMappingStatus.ARCHIVED
            LenientParcelMappingStatus.REOPENED -> {
                when (next) {
                    LenientParcelMappingStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientParcelMappingStatus) {
        if (!canMoveTo(next)) {
            error("lenient parcel mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round(loadKg * weight * 100) / 100

    fun snapshot(): LenientParcelMapping {
        return LenientParcelMapping(
            code = code,
            name = name,
            description = description,
            amount = amount,
            weight = weight,
            loadKg = loadKg,
            title = title,
            currency = currency,
            checksum = checksum,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient parcel mapping in status open. */
fun newLenientParcelMapping(code: String, name: String): LenientParcelMapping {
    return LenientParcelMapping(
        code = code,
        name = name,
        description = "",
        amount = 0.0,
        weight = 0.0,
        loadKg = 0.0,
        title = "",
        currency = "",
        checksum = "",
        category = "",
        status = LenientParcelMappingStatus.OPEN,
    )
}
