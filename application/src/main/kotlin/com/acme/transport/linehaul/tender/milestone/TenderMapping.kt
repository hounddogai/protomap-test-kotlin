package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender mapping is in its life cycle. */
enum class TenderMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tender mapping and its current status. */
data class TenderMapping(
    var code: String,
    var name: String,
    var phone: String,
    var lane: String,
    var expiresAt: Int,
    var tags: MutableList<String>,
    var unit: String,
    var updatedAt: Int,
    var contactEmail: String,
    var status: TenderMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderMappingStatus): Boolean {
        when (status) {
            TenderMappingStatus.NEW -> {
                when (next) {
                    TenderMappingStatus.RUNNING, TenderMappingStatus.DONE -> return true
                }
                return false
            }
            TenderMappingStatus.QUEUED -> return next == TenderMappingStatus.DONE || next == TenderMappingStatus.FAILED
            TenderMappingStatus.RUNNING -> return next == TenderMappingStatus.DONE || next == TenderMappingStatus.FAILED
            TenderMappingStatus.DONE -> return next == TenderMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TenderMappingStatus) {
        if (!canMoveTo(next)) {
            error("tender mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tender mapping"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round((expiresAt.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): TenderMapping {
        return TenderMapping(
            code = code,
            name = name,
            phone = phone,
            lane = lane,
            expiresAt = expiresAt,
            tags = tags,
            unit = unit,
            updatedAt = updatedAt,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tender mapping in status new. */
fun newTenderMapping(code: String, name: String): TenderMapping {
    return TenderMapping(
        code = code,
        name = name,
        phone = "",
        lane = "",
        expiresAt = 0,
        tags = mutableListOf<String>(),
        unit = "",
        updatedAt = 0,
        contactEmail = "",
        status = TenderMappingStatus.NEW,
    )
}
