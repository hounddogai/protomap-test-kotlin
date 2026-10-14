package com.acme.transport.customs.tariff.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default tariff draft is in its life cycle. */
enum class DefaultTariffDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One default tariff draft, with what the planners need to act on it. */
data class DefaultTariffDraft(
    var code: String,
    var title: String,
    var shiftCode: String,
    var contactEmail: String,
    var reviewer: String,
    var distanceKm: Double,
    var margin: Double,
    var category: String,
    var archived: Boolean,
    var createdAt: Int,
    var status: DefaultTariffDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTariffDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTariffDraftStatus): Boolean {
        when (status) {
            DefaultTariffDraftStatus.CREATED -> {
                when (next) {
                    DefaultTariffDraftStatus.VALIDATED -> return true
                }
                return false
            }
            DefaultTariffDraftStatus.VALIDATED -> {
                when (next) {
                    DefaultTariffDraftStatus.PUBLISHED, DefaultTariffDraftStatus.RETIRED -> return true
                }
                return false
            }
            DefaultTariffDraftStatus.PUBLISHED -> return next == DefaultTariffDraftStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultTariffDraftStatus) {
        if (!canMoveTo(next)) {
            error("default tariff draft $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveLoad(): Double = round((margin - distanceKm) * 100) / 100

    fun snapshot(): DefaultTariffDraft {
        return DefaultTariffDraft(
            code = code,
            title = title,
            shiftCode = shiftCode,
            contactEmail = contactEmail,
            reviewer = reviewer,
            distanceKm = distanceKm,
            margin = margin,
            category = category,
            archived = archived,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default tariff draft in status created. */
fun newDefaultTariffDraft(code: String, title: String): DefaultTariffDraft {
    return DefaultTariffDraft(
        code = code,
        title = title,
        shiftCode = "",
        contactEmail = "",
        reviewer = "",
        distanceKm = 0.0,
        margin = 0.0,
        category = "",
        archived = false,
        createdAt = 0,
        status = DefaultTariffDraftStatus.CREATED,
    )
}
