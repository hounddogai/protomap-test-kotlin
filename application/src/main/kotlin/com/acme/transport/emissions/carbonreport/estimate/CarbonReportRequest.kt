package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report request is in its life cycle. */
enum class CarbonReportRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A carbon report request. Its code is unique within a tenant. */
data class CarbonReportRequest(
    var code: String,
    var title: String,
    var checksum: String,
    var loadKg: Double,
    var updatedAt: Int,
    var status: CarbonReportRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportRequestStatus): Boolean {
        when (status) {
            CarbonReportRequestStatus.PLANNED -> {
                when (next) {
                    CarbonReportRequestStatus.COMPLETED -> return true
                }
                return false
            }
            CarbonReportRequestStatus.SCHEDULED -> {
                when (next) {
                    CarbonReportRequestStatus.CANCELLED -> return true
                }
                return false
            }
            CarbonReportRequestStatus.IN_PROGRESS -> {
                when (next) {
                    CarbonReportRequestStatus.COMPLETED, CarbonReportRequestStatus.CANCELLED -> return true
                }
                return false
            }
            CarbonReportRequestStatus.COMPLETED -> {
                when (next) {
                    CarbonReportRequestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportRequestStatus) {
        if (!canMoveTo(next)) {
            error("carbon report request $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netTotal(): Double = round((loadKg + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): CarbonReportRequest {
        return CarbonReportRequest(
            code = code,
            title = title,
            checksum = checksum,
            loadKg = loadKg,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a carbon report request in status planned. */
fun newCarbonReportRequest(code: String, title: String): CarbonReportRequest {
    return CarbonReportRequest(
        code = code,
        title = title,
        checksum = "",
        loadKg = 0.0,
        updatedAt = 0,
        status = CarbonReportRequestStatus.PLANNED,
    )
}
