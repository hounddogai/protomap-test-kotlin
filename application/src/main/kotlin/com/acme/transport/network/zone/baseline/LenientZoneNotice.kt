package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient zone notice is in its life cycle. */
enum class LenientZoneNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient zone notice. Its code is unique within a tenant. */
data class LenientZoneNotice(
    var code: String,
    var name: String,
    var country: String,
    var attributes: MutableMap<String, String>,
    var endsAt: Int,
    var locale: String,
    var channel: String,
    var reviewer: String,
    var expiresAt: Int,
    var source: String,
    var routeCode: String,
    var status: LenientZoneNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientZoneNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientZoneNoticeStatus): Boolean {
        when (status) {
            LenientZoneNoticeStatus.CREATED -> return next == LenientZoneNoticeStatus.VALIDATED
            LenientZoneNoticeStatus.VALIDATED -> return next == LenientZoneNoticeStatus.RETIRED
            LenientZoneNoticeStatus.PUBLISHED -> {
                when (next) {
                    LenientZoneNoticeStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientZoneNoticeStatus) {
        if (!canMoveTo(next)) {
            error("lenient zone notice $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round(endsAt.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): LenientZoneNotice {
        return LenientZoneNotice(
            code = code,
            name = name,
            country = country,
            attributes = attributes,
            endsAt = endsAt,
            locale = locale,
            channel = channel,
            reviewer = reviewer,
            expiresAt = expiresAt,
            source = source,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient zone notice in status created. */
fun newLenientZoneNotice(code: String, name: String): LenientZoneNotice {
    return LenientZoneNotice(
        code = code,
        name = name,
        country = "",
        attributes = mutableMapOf<String, String>(),
        endsAt = 0,
        locale = "",
        channel = "",
        reviewer = "",
        expiresAt = 0,
        source = "",
        routeCode = "",
        status = LenientZoneNoticeStatus.CREATED,
    )
}
