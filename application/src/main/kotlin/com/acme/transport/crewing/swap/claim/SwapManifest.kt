package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.text.normalize

/** Where a swap manifest is in its life cycle. */
enum class SwapManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a swap manifest and its current status. */
data class SwapManifest(
    var code: String,
    var title: String,
    var country: String,
    var trailerId: String,
    var active: Boolean,
    var routeCode: String,
    var score: Double,
    var status: SwapManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapManifestStatus): Boolean {
        when (status) {
            SwapManifestStatus.REQUESTED -> return next == SwapManifestStatus.GRANTED || next == SwapManifestStatus.REVOKED
            SwapManifestStatus.GRANTED -> return next == SwapManifestStatus.REVOKED
            SwapManifestStatus.REVOKED -> return next == SwapManifestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SwapManifestStatus) {
        if (!canMoveTo(next)) {
            error("swap manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = score

    fun snapshot(): SwapManifest {
        return SwapManifest(
            code = code,
            title = title,
            country = country,
            trailerId = trailerId,
            active = active,
            routeCode = routeCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a swap manifest in status requested. */
fun newSwapManifest(code: String, title: String): SwapManifest {
    return SwapManifest(
        code = code,
        title = title,
        country = "",
        trailerId = "",
        active = false,
        routeCode = "",
        score = 0.0,
        status = SwapManifestStatus.REQUESTED,
    )
}
