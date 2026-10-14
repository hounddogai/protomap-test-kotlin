package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report draft is in its life cycle. */
enum class CarbonReportDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a carbon report draft and its current status. */
data class CarbonReportDraft(
    var code: String,
    var name: String,
    var threshold: Double,
    var summary: String,
    var depot: String,
    var version: Int,
    var cost: Double,
    var tags: MutableList<String>,
    var retries: Int,
    var currency: String,
    var status: CarbonReportDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportDraftStatus): Boolean {
        when (status) {
            CarbonReportDraftStatus.OPEN -> return next == CarbonReportDraftStatus.REOPENED
            CarbonReportDraftStatus.IN_REVIEW -> return next == CarbonReportDraftStatus.RESOLVED
            CarbonReportDraftStatus.RESOLVED -> {
                when (next) {
                    CarbonReportDraftStatus.REOPENED, CarbonReportDraftStatus.ARCHIVED -> return true
                }
                return false
            }
            CarbonReportDraftStatus.REOPENED -> {
                when (next) {
                    CarbonReportDraftStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportDraftStatus) {
        if (!canMoveTo(next)) {
            error("carbon report draft $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round((threshold + retries.toDouble()) * 100) / 100

    fun snapshot(): CarbonReportDraft {
        return CarbonReportDraft(
            code = code,
            name = name,
            threshold = threshold,
            summary = summary,
            depot = depot,
            version = version,
            cost = cost,
            tags = tags,
            retries = retries,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a carbon report draft in status open. */
fun newCarbonReportDraft(code: String, name: String): CarbonReportDraft {
    return CarbonReportDraft(
        code = code,
        name = name,
        threshold = 0.0,
        summary = "",
        depot = "",
        version = 0,
        cost = 0.0,
        tags = mutableListOf<String>(),
        retries = 0,
        currency = "",
        status = CarbonReportDraftStatus.OPEN,
    )
}
