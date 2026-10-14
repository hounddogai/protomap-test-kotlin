package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor bundle is in its life cycle. */
enum class EmissionFactorBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One emission factor bundle, with what the planners need to act on it. */
data class EmissionFactorBundle(
    var code: String,
    var title: String,
    var label: String,
    var name: String,
    var pinned: Boolean,
    var margin: Double,
    var quantity: Int,
    var source: String,
    var attributes: MutableMap<String, String>,
    var status: EmissionFactorBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorBundleStatus): Boolean {
        when (status) {
            EmissionFactorBundleStatus.OPEN -> return next == EmissionFactorBundleStatus.IN_REVIEW
            EmissionFactorBundleStatus.IN_REVIEW -> {
                when (next) {
                    EmissionFactorBundleStatus.REOPENED, EmissionFactorBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            EmissionFactorBundleStatus.RESOLVED -> {
                when (next) {
                    EmissionFactorBundleStatus.REOPENED -> return true
                }
                return false
            }
            EmissionFactorBundleStatus.REOPENED -> {
                when (next) {
                    EmissionFactorBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorBundleStatus) {
        if (!canMoveTo(next)) {
            error("emission factor bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"emission factor bundle"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round(quantity.toDouble() * margin * 100) / 100

    fun snapshot(): EmissionFactorBundle {
        return EmissionFactorBundle(
            code = code,
            title = title,
            label = label,
            name = name,
            pinned = pinned,
            margin = margin,
            quantity = quantity,
            source = source,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a emission factor bundle in status open. */
fun newEmissionFactorBundle(code: String, title: String): EmissionFactorBundle {
    return EmissionFactorBundle(
        code = code,
        title = title,
        label = "",
        name = "",
        pinned = false,
        margin = 0.0,
        quantity = 0,
        source = "",
        attributes = mutableMapOf<String, String>(),
        status = EmissionFactorBundleStatus.OPEN,
    )
}
