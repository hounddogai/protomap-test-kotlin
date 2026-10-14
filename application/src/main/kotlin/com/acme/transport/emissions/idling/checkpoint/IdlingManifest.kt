package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling manifest is in its life cycle. */
enum class IdlingManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A idling manifest as the idling team keeps it. */
data class IdlingManifest(
    var code: String,
    var title: String,
    var revision: Int,
    var priority: Int,
    var active: Boolean,
    var locale: String,
    var checksum: String,
    var timezone: String,
    var windowDays: Int,
    var durationMinutes: Int,
    var status: IdlingManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingManifestStatus): Boolean {
        when (status) {
            IdlingManifestStatus.CREATED -> {
                when (next) {
                    IdlingManifestStatus.VALIDATED -> return true
                }
                return false
            }
            IdlingManifestStatus.VALIDATED -> return next == IdlingManifestStatus.RETIRED
            IdlingManifestStatus.PUBLISHED -> return next == IdlingManifestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: IdlingManifestStatus) {
        if (!canMoveTo(next)) {
            error("idling manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round((revision.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): IdlingManifest {
        return IdlingManifest(
            code = code,
            title = title,
            revision = revision,
            priority = priority,
            active = active,
            locale = locale,
            checksum = checksum,
            timezone = timezone,
            windowDays = windowDays,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a idling manifest in status created. */
fun newIdlingManifest(code: String, title: String): IdlingManifest {
    return IdlingManifest(
        code = code,
        title = title,
        revision = 0,
        priority = 0,
        active = false,
        locale = "",
        checksum = "",
        timezone = "",
        windowDays = 0,
        durationMinutes = 0,
        status = IdlingManifestStatus.CREATED,
    )
}
