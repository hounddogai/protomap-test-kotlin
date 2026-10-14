package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.text.normalize

/** Where a lenient hub manifest is in its life cycle. */
enum class LenientHubManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient hub manifest. Its code is unique within a tenant. */
data class LenientHubManifest(
    var code: String,
    var title: String,
    var floorValue: Double,
    var timezone: String,
    var locale: String,
    var status: LenientHubManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientHubManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientHubManifestStatus): Boolean {
        when (status) {
            LenientHubManifestStatus.NEW -> {
                when (next) {
                    LenientHubManifestStatus.QUEUED, LenientHubManifestStatus.DONE -> return true
                }
                return false
            }
            LenientHubManifestStatus.QUEUED -> {
                when (next) {
                    LenientHubManifestStatus.FAILED -> return true
                }
                return false
            }
            LenientHubManifestStatus.RUNNING -> {
                when (next) {
                    LenientHubManifestStatus.DONE, LenientHubManifestStatus.FAILED -> return true
                }
                return false
            }
            LenientHubManifestStatus.DONE -> {
                when (next) {
                    LenientHubManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientHubManifestStatus) {
        if (!canMoveTo(next)) {
            error("lenient hub manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient hub manifest"} $code"

    fun measuredValue(): Double = floorValue

    fun snapshot(): LenientHubManifest {
        return LenientHubManifest(
            code = code,
            title = title,
            floorValue = floorValue,
            timezone = timezone,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lenient hub manifest in status new. */
fun newLenientHubManifest(code: String, title: String): LenientHubManifest {
    return LenientHubManifest(
        code = code,
        title = title,
        floorValue = 0.0,
        timezone = "",
        locale = "",
        status = LenientHubManifestStatus.NEW,
    )
}
