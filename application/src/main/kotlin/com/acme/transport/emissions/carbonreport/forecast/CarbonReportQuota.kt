package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.kit.text.normalize

/** Where a carbon report quota is in its life cycle. */
enum class CarbonReportQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a carbon report quota and its current status. */
data class CarbonReportQuota(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var dock: String,
    var attributes: MutableMap<String, String>,
    var score: Double,
    var status: CarbonReportQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportQuotaStatus): Boolean {
        when (status) {
            CarbonReportQuotaStatus.PROPOSED -> {
                when (next) {
                    CarbonReportQuotaStatus.REJECTED, CarbonReportQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            CarbonReportQuotaStatus.APPROVED -> {
                when (next) {
                    CarbonReportQuotaStatus.REJECTED, CarbonReportQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            CarbonReportQuotaStatus.REJECTED -> {
                when (next) {
                    CarbonReportQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportQuotaStatus) {
        if (!canMoveTo(next)) {
            error("carbon report quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"carbon report quota"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = score

    fun snapshot(): CarbonReportQuota {
        return CarbonReportQuota(
            code = code,
            title = title,
            pinned = pinned,
            dock = dock,
            attributes = attributes,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a carbon report quota in status proposed. */
fun newCarbonReportQuota(code: String, title: String): CarbonReportQuota {
    return CarbonReportQuota(
        code = code,
        title = title,
        pinned = false,
        dock = "",
        attributes = mutableMapOf<String, String>(),
        score = 0.0,
        status = CarbonReportQuotaStatus.PROPOSED,
    )
}
