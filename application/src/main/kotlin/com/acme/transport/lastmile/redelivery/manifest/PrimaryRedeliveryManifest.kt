package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary redelivery manifest records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a primary redelivery manifest is in its life cycle. */
enum class PrimaryRedeliveryManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One primary redelivery manifest, with what the planners need to act on it. */
data class PrimaryRedeliveryManifest(
    var code: String,
    var title: String,
    var maximum: Double,
    var graceDays: Int,
    var tags: MutableList<String>,
    var externalRef: String,
    var description: String,
    var endsAt: Int,
    var channel: String,
    var status: PrimaryRedeliveryManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRedeliveryManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRedeliveryManifestStatus): Boolean {
        when (status) {
            PrimaryRedeliveryManifestStatus.CREATED -> return next == PrimaryRedeliveryManifestStatus.VALIDATED || next == PrimaryRedeliveryManifestStatus.RETIRED
            PrimaryRedeliveryManifestStatus.VALIDATED -> {
                when (next) {
                    PrimaryRedeliveryManifestStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryRedeliveryManifestStatus.PUBLISHED -> return next == PrimaryRedeliveryManifestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryRedeliveryManifestStatus) {
        if (!canMoveTo(next)) {
            error("primary redelivery manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round(graceDays.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): PrimaryRedeliveryManifest {
        return PrimaryRedeliveryManifest(
            code = code,
            title = title,
            maximum = maximum,
            graceDays = graceDays,
            tags = tags,
            externalRef = externalRef,
            description = description,
            endsAt = endsAt,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary redelivery manifest in status created. */
fun newPrimaryRedeliveryManifest(code: String, title: String): PrimaryRedeliveryManifest {
    return PrimaryRedeliveryManifest(
        code = code,
        title = title,
        maximum = 0.0,
        graceDays = 0,
        tags = mutableListOf<String>(),
        externalRef = "",
        description = "",
        endsAt = 0,
        channel = "",
        status = PrimaryRedeliveryManifestStatus.CREATED,
    )
}
