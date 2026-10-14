package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling qualification notice is in its life cycle. */
enum class RollingQualificationNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling qualification notice. Its code is unique within a tenant. */
data class RollingQualificationNotice(
    var code: String,
    var name: String,
    var minimum: Double,
    var attempts: Int,
    var odometer: Int,
    var locale: String,
    var contactEmail: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var status: RollingQualificationNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingQualificationNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingQualificationNoticeStatus): Boolean {
        when (status) {
            RollingQualificationNoticeStatus.CREATED -> {
                when (next) {
                    RollingQualificationNoticeStatus.RETIRED -> return true
                }
                return false
            }
            RollingQualificationNoticeStatus.VALIDATED -> {
                when (next) {
                    RollingQualificationNoticeStatus.PUBLISHED, RollingQualificationNoticeStatus.RETIRED -> return true
                }
                return false
            }
            RollingQualificationNoticeStatus.PUBLISHED -> return next == RollingQualificationNoticeStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingQualificationNoticeStatus) {
        if (!canMoveTo(next)) {
            error("rolling qualification notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((minimum + attempts.toDouble()) * 100) / 100

    fun snapshot(): RollingQualificationNotice {
        return RollingQualificationNotice(
            code = code,
            name = name,
            minimum = minimum,
            attempts = attempts,
            odometer = odometer,
            locale = locale,
            contactEmail = contactEmail,
            label = label,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling qualification notice in status created. */
fun newRollingQualificationNotice(code: String, name: String): RollingQualificationNotice {
    return RollingQualificationNotice(
        code = code,
        name = name,
        minimum = 0.0,
        attempts = 0,
        odometer = 0,
        locale = "",
        contactEmail = "",
        label = "",
        attributes = mutableMapOf<String, String>(),
        status = RollingQualificationNoticeStatus.CREATED,
    )
}
