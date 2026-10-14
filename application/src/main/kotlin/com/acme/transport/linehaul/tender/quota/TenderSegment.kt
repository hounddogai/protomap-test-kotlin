package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender segment is in its life cycle. */
enum class TenderSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tender segment. Its code is unique within a tenant. */
data class TenderSegment(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var description: String,
    var ownerId: String,
    var sequence: Int,
    var weight: Double,
    var endsAt: Int,
    var phone: String,
    var segment: String,
    var status: TenderSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderSegmentStatus): Boolean {
        when (status) {
            TenderSegmentStatus.CREATED -> {
                when (next) {
                    TenderSegmentStatus.VALIDATED, TenderSegmentStatus.PUBLISHED -> return true
                }
                return false
            }
            TenderSegmentStatus.VALIDATED -> return next == TenderSegmentStatus.PUBLISHED
            TenderSegmentStatus.PUBLISHED -> {
                when (next) {
                    TenderSegmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderSegmentStatus) {
        if (!canMoveTo(next)) {
            error("tender segment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((weight - endsAt.toDouble()) * 100) / 100

    fun snapshot(): TenderSegment {
        return TenderSegment(
            code = code,
            label = label,
            attributes = attributes,
            description = description,
            ownerId = ownerId,
            sequence = sequence,
            weight = weight,
            endsAt = endsAt,
            phone = phone,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tender segment in status created. */
fun newTenderSegment(code: String, label: String): TenderSegment {
    return TenderSegment(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        description = "",
        ownerId = "",
        sequence = 0,
        weight = 0.0,
        endsAt = 0,
        phone = "",
        segment = "",
        status = TenderSegmentStatus.CREATED,
    )
}
