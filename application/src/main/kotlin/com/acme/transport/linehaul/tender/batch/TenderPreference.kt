package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender preference is in its life cycle. */
enum class TenderPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tender preference as the tender team keeps it. */
data class TenderPreference(
    var code: String,
    var label: String,
    var batchSize: Int,
    var category: String,
    var currency: String,
    var ceiling: Double,
    var cost: Double,
    var depot: String,
    var ratio: Double,
    var status: TenderPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderPreferenceStatus): Boolean {
        when (status) {
            TenderPreferenceStatus.PLANNED -> {
                when (next) {
                    TenderPreferenceStatus.SCHEDULED, TenderPreferenceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TenderPreferenceStatus.SCHEDULED -> {
                when (next) {
                    TenderPreferenceStatus.IN_PROGRESS, TenderPreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            TenderPreferenceStatus.IN_PROGRESS -> {
                when (next) {
                    TenderPreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            TenderPreferenceStatus.COMPLETED -> {
                when (next) {
                    TenderPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("tender preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedVolume(): Double = round((ceiling - ratio) * 100) / 100

    fun snapshot(): TenderPreference {
        return TenderPreference(
            code = code,
            label = label,
            batchSize = batchSize,
            category = category,
            currency = currency,
            ceiling = ceiling,
            cost = cost,
            depot = depot,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tender preference in status planned. */
fun newTenderPreference(code: String, label: String): TenderPreference {
    return TenderPreference(
        code = code,
        label = label,
        batchSize = 0,
        category = "",
        currency = "",
        ceiling = 0.0,
        cost = 0.0,
        depot = "",
        ratio = 0.0,
        status = TenderPreferenceStatus.PLANNED,
    )
}
