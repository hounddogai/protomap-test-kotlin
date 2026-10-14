package com.acme.transport.crewing.swap.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The swap mapping records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap mapping is in its life cycle. */
enum class SwapMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One swap mapping, with what the planners need to act on it. */
data class SwapMapping(
    var code: String,
    var label: String,
    var ceiling: Double,
    var score: Double,
    var expiresAt: Int,
    var loadKg: Double,
    var margin: Double,
    var floorValue: Double,
    var checksum: String,
    var status: SwapMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapMappingStatus): Boolean {
        when (status) {
            SwapMappingStatus.REQUESTED -> return next == SwapMappingStatus.REVOKED || next == SwapMappingStatus.EXPIRED
            SwapMappingStatus.GRANTED -> return next == SwapMappingStatus.REVOKED || next == SwapMappingStatus.EXPIRED
            SwapMappingStatus.REVOKED -> {
                when (next) {
                    SwapMappingStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapMappingStatus) {
        if (!canMoveTo(next)) {
            error("swap mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedExposure(): Double = round((margin + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): SwapMapping {
        return SwapMapping(
            code = code,
            label = label,
            ceiling = ceiling,
            score = score,
            expiresAt = expiresAt,
            loadKg = loadKg,
            margin = margin,
            floorValue = floorValue,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a swap mapping in status requested. */
fun newSwapMapping(code: String, label: String): SwapMapping {
    return SwapMapping(
        code = code,
        label = label,
        ceiling = 0.0,
        score = 0.0,
        expiresAt = 0,
        loadKg = 0.0,
        margin = 0.0,
        floorValue = 0.0,
        checksum = "",
        status = SwapMappingStatus.REQUESTED,
    )
}
