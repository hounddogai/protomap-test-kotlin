package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback hub mapping is in its life cycle. */
enum class FallbackHubMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback hub mapping. Its code is unique within a tenant. */
data class FallbackHubMapping(
    var code: String,
    var title: String,
    var checksum: String,
    var etaMinutes: Int,
    var vehicleId: String,
    var durationMinutes: Int,
    var weight: Double,
    var expiresAt: Int,
    var lane: String,
    var segment: String,
    var score: Double,
    var status: FallbackHubMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackHubMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackHubMappingStatus): Boolean {
        when (status) {
            FallbackHubMappingStatus.OPEN -> return next == FallbackHubMappingStatus.IN_REVIEW || next == FallbackHubMappingStatus.ARCHIVED
            FallbackHubMappingStatus.IN_REVIEW -> return next == FallbackHubMappingStatus.REOPENED || next == FallbackHubMappingStatus.ARCHIVED
            FallbackHubMappingStatus.RESOLVED -> return next == FallbackHubMappingStatus.REOPENED || next == FallbackHubMappingStatus.ARCHIVED
            FallbackHubMappingStatus.REOPENED -> return next == FallbackHubMappingStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackHubMappingStatus) {
        if (!canMoveTo(next)) {
            error("fallback hub mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round((score + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): FallbackHubMapping {
        return FallbackHubMapping(
            code = code,
            title = title,
            checksum = checksum,
            etaMinutes = etaMinutes,
            vehicleId = vehicleId,
            durationMinutes = durationMinutes,
            weight = weight,
            expiresAt = expiresAt,
            lane = lane,
            segment = segment,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback hub mapping in status open. */
fun newFallbackHubMapping(code: String, title: String): FallbackHubMapping {
    return FallbackHubMapping(
        code = code,
        title = title,
        checksum = "",
        etaMinutes = 0,
        vehicleId = "",
        durationMinutes = 0,
        weight = 0.0,
        expiresAt = 0,
        lane = "",
        segment = "",
        score = 0.0,
        status = FallbackHubMappingStatus.OPEN,
    )
}
