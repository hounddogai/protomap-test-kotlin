package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor digest is in its life cycle. */
enum class EmissionFactorDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A emission factor digest as the emission factor team keeps it. */
data class EmissionFactorDigest(
    var code: String,
    var title: String,
    var label: String,
    var archived: Boolean,
    var version: Int,
    var pinned: Boolean,
    var source: String,
    var summary: String,
    var category: String,
    var minimum: Double,
    var status: EmissionFactorDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorDigestStatus): Boolean {
        when (status) {
            EmissionFactorDigestStatus.DRAFT -> {
                when (next) {
                    EmissionFactorDigestStatus.SUSPENDED, EmissionFactorDigestStatus.CLOSED -> return true
                }
                return false
            }
            EmissionFactorDigestStatus.PENDING -> {
                when (next) {
                    EmissionFactorDigestStatus.CLOSED -> return true
                }
                return false
            }
            EmissionFactorDigestStatus.ACTIVE -> return next == EmissionFactorDigestStatus.CLOSED
            EmissionFactorDigestStatus.SUSPENDED -> {
                when (next) {
                    EmissionFactorDigestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorDigestStatus) {
        if (!canMoveTo(next)) {
            error("emission factor digest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($title)"

    fun projectedValue(): Double = round(minimum * version.toDouble() * 100) / 100

    fun snapshot(): EmissionFactorDigest {
        return EmissionFactorDigest(
            code = code,
            title = title,
            label = label,
            archived = archived,
            version = version,
            pinned = pinned,
            source = source,
            summary = summary,
            category = category,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a emission factor digest in status draft. */
fun newEmissionFactorDigest(code: String, title: String): EmissionFactorDigest {
    return EmissionFactorDigest(
        code = code,
        title = title,
        label = "",
        archived = false,
        version = 0,
        pinned = false,
        source = "",
        summary = "",
        category = "",
        minimum = 0.0,
        status = EmissionFactorDigestStatus.DRAFT,
    )
}
