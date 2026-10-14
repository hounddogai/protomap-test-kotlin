package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient node manifest is in its life cycle. */
enum class LenientNodeManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient node manifest as the node team keeps it. */
data class LenientNodeManifest(
    var code: String,
    var name: String,
    var rate: Double,
    var archived: Boolean,
    var score: Double,
    var threshold: Double,
    var minimum: Double,
    var currency: String,
    var status: LenientNodeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNodeManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNodeManifestStatus): Boolean {
        when (status) {
            LenientNodeManifestStatus.REQUESTED -> {
                when (next) {
                    LenientNodeManifestStatus.GRANTED, LenientNodeManifestStatus.REVOKED -> return true
                }
                return false
            }
            LenientNodeManifestStatus.GRANTED -> return next == LenientNodeManifestStatus.REVOKED
            LenientNodeManifestStatus.REVOKED -> return next == LenientNodeManifestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientNodeManifestStatus) {
        if (!canMoveTo(next)) {
            error("lenient node manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netExposure(): Double = round(threshold * score * 100) / 100

    fun snapshot(): LenientNodeManifest {
        return LenientNodeManifest(
            code = code,
            name = name,
            rate = rate,
            archived = archived,
            score = score,
            threshold = threshold,
            minimum = minimum,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient node manifest in status requested. */
fun newLenientNodeManifest(code: String, name: String): LenientNodeManifest {
    return LenientNodeManifest(
        code = code,
        name = name,
        rate = 0.0,
        archived = false,
        score = 0.0,
        threshold = 0.0,
        minimum = 0.0,
        currency = "",
        status = LenientNodeManifestStatus.REQUESTED,
    )
}
