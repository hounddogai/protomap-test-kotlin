package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback trailer yard bundle is in its life cycle. */
enum class FallbackTrailerYardBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback trailer yard bundle. Its code is unique within a tenant. */
data class FallbackTrailerYardBundle(
    var code: String,
    var name: String,
    var stopCount: Int,
    var summary: String,
    var visible: Boolean,
    var endsAt: Int,
    var minimum: Double,
    var budget: Double,
    var status: FallbackTrailerYardBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTrailerYardBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTrailerYardBundleStatus): Boolean {
        when (status) {
            FallbackTrailerYardBundleStatus.OPEN -> {
                when (next) {
                    FallbackTrailerYardBundleStatus.IN_REVIEW, FallbackTrailerYardBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackTrailerYardBundleStatus.IN_REVIEW -> {
                when (next) {
                    FallbackTrailerYardBundleStatus.RESOLVED, FallbackTrailerYardBundleStatus.REOPENED -> return true
                }
                return false
            }
            FallbackTrailerYardBundleStatus.RESOLVED -> {
                when (next) {
                    FallbackTrailerYardBundleStatus.REOPENED, FallbackTrailerYardBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackTrailerYardBundleStatus.REOPENED -> {
                when (next) {
                    FallbackTrailerYardBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackTrailerYardBundleStatus) {
        if (!canMoveTo(next)) {
            error("fallback trailer yard bundle $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netLoad(): Double = round((endsAt.toDouble() - minimum) * 100) / 100

    fun snapshot(): FallbackTrailerYardBundle {
        return FallbackTrailerYardBundle(
            code = code,
            name = name,
            stopCount = stopCount,
            summary = summary,
            visible = visible,
            endsAt = endsAt,
            minimum = minimum,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback trailer yard bundle in status open. */
fun newFallbackTrailerYardBundle(code: String, name: String): FallbackTrailerYardBundle {
    return FallbackTrailerYardBundle(
        code = code,
        name = name,
        stopCount = 0,
        summary = "",
        visible = false,
        endsAt = 0,
        minimum = 0.0,
        budget = 0.0,
        status = FallbackTrailerYardBundleStatus.OPEN,
    )
}
