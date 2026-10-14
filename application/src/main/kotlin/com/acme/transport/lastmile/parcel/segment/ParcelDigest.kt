package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel digest is in its life cycle. */
enum class ParcelDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A parcel digest as the parcel team keeps it. */
data class ParcelDigest(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var tags: MutableList<String>,
    var version: Int,
    var visible: Boolean,
    var country: String,
    var startsAt: Int,
    var status: ParcelDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelDigestStatus): Boolean {
        when (status) {
            ParcelDigestStatus.CREATED -> return next == ParcelDigestStatus.VALIDATED || next == ParcelDigestStatus.PUBLISHED
            ParcelDigestStatus.VALIDATED -> return next == ParcelDigestStatus.RETIRED
            ParcelDigestStatus.PUBLISHED -> return next == ParcelDigestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ParcelDigestStatus) {
        if (!canMoveTo(next)) {
            error("parcel digest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"parcel digest"} $code"

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

    fun effectiveLoad(): Double = round((version.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): ParcelDigest {
        return ParcelDigest(
            code = code,
            title = title,
            attributes = attributes,
            tags = tags,
            version = version,
            visible = visible,
            country = country,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a parcel digest in status created. */
fun newParcelDigest(code: String, title: String): ParcelDigest {
    return ParcelDigest(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        tags = mutableListOf<String>(),
        version = 0,
        visible = false,
        country = "",
        startsAt = 0,
        status = ParcelDigestStatus.CREATED,
    )
}
