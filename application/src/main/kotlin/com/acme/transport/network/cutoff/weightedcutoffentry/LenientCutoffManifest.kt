package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient cutoff manifest is in its life cycle. */
enum class LenientCutoffManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient cutoff manifest. Its code is unique within a tenant. */
data class LenientCutoffManifest(
    var code: String,
    var name: String,
    var source: String,
    var tags: MutableList<String>,
    var discount: Double,
    var notes: String,
    var attributes: MutableMap<String, String>,
    var amount: Double,
    var ceiling: Double,
    var phone: String,
    var channel: String,
    var status: LenientCutoffManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCutoffManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCutoffManifestStatus): Boolean {
        when (status) {
            LenientCutoffManifestStatus.CREATED -> return next == LenientCutoffManifestStatus.PUBLISHED || next == LenientCutoffManifestStatus.RETIRED
            LenientCutoffManifestStatus.VALIDATED -> {
                when (next) {
                    LenientCutoffManifestStatus.PUBLISHED, LenientCutoffManifestStatus.RETIRED -> return true
                }
                return false
            }
            LenientCutoffManifestStatus.PUBLISHED -> {
                when (next) {
                    LenientCutoffManifestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientCutoffManifestStatus) {
        if (!canMoveTo(next)) {
            error("lenient cutoff manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round(amount * discount * 100) / 100

    fun snapshot(): LenientCutoffManifest {
        return LenientCutoffManifest(
            code = code,
            name = name,
            source = source,
            tags = tags,
            discount = discount,
            notes = notes,
            attributes = attributes,
            amount = amount,
            ceiling = ceiling,
            phone = phone,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient cutoff manifest in status created. */
fun newLenientCutoffManifest(code: String, name: String): LenientCutoffManifest {
    return LenientCutoffManifest(
        code = code,
        name = name,
        source = "",
        tags = mutableListOf<String>(),
        discount = 0.0,
        notes = "",
        attributes = mutableMapOf<String, String>(),
        amount = 0.0,
        ceiling = 0.0,
        phone = "",
        channel = "",
        status = LenientCutoffManifestStatus.CREATED,
    )
}
