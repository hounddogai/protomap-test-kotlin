package com.acme.transport.safety.cameraevent.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a camera event template is in its life cycle. */
enum class CameraEventTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One camera event template, with what the planners need to act on it. */
data class CameraEventTemplate(
    var code: String,
    var label: String,
    var revision: Int,
    var pinned: Boolean,
    var limit: Int,
    var country: String,
    var currency: String,
    var attributes: MutableMap<String, String>,
    var expiresAt: Int,
    var category: String,
    var lane: String,
    var status: CameraEventTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CameraEventTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CameraEventTemplateStatus): Boolean {
        when (status) {
            CameraEventTemplateStatus.DRAFT -> {
                when (next) {
                    CameraEventTemplateStatus.PENDING, CameraEventTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            CameraEventTemplateStatus.PENDING -> {
                when (next) {
                    CameraEventTemplateStatus.ACTIVE, CameraEventTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            CameraEventTemplateStatus.ACTIVE -> {
                when (next) {
                    CameraEventTemplateStatus.CLOSED -> return true
                }
                return false
            }
            CameraEventTemplateStatus.SUSPENDED -> return next == CameraEventTemplateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CameraEventTemplateStatus) {
        if (!canMoveTo(next)) {
            error("camera event template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round(expiresAt.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): CameraEventTemplate {
        return CameraEventTemplate(
            code = code,
            label = label,
            revision = revision,
            pinned = pinned,
            limit = limit,
            country = country,
            currency = currency,
            attributes = attributes,
            expiresAt = expiresAt,
            category = category,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a camera event template in status draft. */
fun newCameraEventTemplate(code: String, label: String): CameraEventTemplate {
    return CameraEventTemplate(
        code = code,
        label = label,
        revision = 0,
        pinned = false,
        limit = 0,
        country = "",
        currency = "",
        attributes = mutableMapOf<String, String>(),
        expiresAt = 0,
        category = "",
        lane = "",
        status = CameraEventTemplateStatus.DRAFT,
    )
}
