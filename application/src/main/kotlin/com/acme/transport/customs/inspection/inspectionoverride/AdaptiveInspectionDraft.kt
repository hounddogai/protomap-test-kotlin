package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection draft is in its life cycle. */
enum class AdaptiveInspectionDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive inspection draft. Its code is unique within a tenant. */
data class AdaptiveInspectionDraft(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var loadKg: Double,
    var country: String,
    var depot: String,
    var status: AdaptiveInspectionDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionDraftStatus): Boolean {
        when (status) {
            AdaptiveInspectionDraftStatus.DRAFT -> {
                when (next) {
                    AdaptiveInspectionDraftStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveInspectionDraftStatus.PENDING -> {
                when (next) {
                    AdaptiveInspectionDraftStatus.SUSPENDED, AdaptiveInspectionDraftStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveInspectionDraftStatus.ACTIVE -> {
                when (next) {
                    AdaptiveInspectionDraftStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveInspectionDraftStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveInspectionDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionDraftStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive inspection draft"} $code"

    fun weightedVolume(): Double = round((loadKg + fuelLiters) * 100) / 100

    fun snapshot(): AdaptiveInspectionDraft {
        return AdaptiveInspectionDraft(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            loadKg = loadKg,
            country = country,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive inspection draft in status draft. */
fun newAdaptiveInspectionDraft(code: String, name: String): AdaptiveInspectionDraft {
    return AdaptiveInspectionDraft(
        code = code,
        name = name,
        fuelLiters = 0.0,
        loadKg = 0.0,
        country = "",
        depot = "",
        status = AdaptiveInspectionDraftStatus.DRAFT,
    )
}
