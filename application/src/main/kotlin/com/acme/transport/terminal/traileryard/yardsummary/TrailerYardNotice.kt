package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard notice is in its life cycle. */
enum class TrailerYardNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A trailer yard notice. Its code is unique within a tenant. */
data class TrailerYardNotice(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var attributes: MutableMap<String, String>,
    var pallets: Int,
    var depot: String,
    var status: TrailerYardNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardNoticeStatus): Boolean {
        when (status) {
            TrailerYardNoticeStatus.CREATED -> return next == TrailerYardNoticeStatus.VALIDATED || next == TrailerYardNoticeStatus.PUBLISHED
            TrailerYardNoticeStatus.VALIDATED -> return next == TrailerYardNoticeStatus.PUBLISHED || next == TrailerYardNoticeStatus.RETIRED
            TrailerYardNoticeStatus.PUBLISHED -> return next == TrailerYardNoticeStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TrailerYardNoticeStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard notice $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round((expiresAt.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardNotice {
        return TrailerYardNotice(
            code = code,
            title = title,
            expiresAt = expiresAt,
            attributes = attributes,
            pallets = pallets,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a trailer yard notice in status created. */
fun newTrailerYardNotice(code: String, title: String): TrailerYardNotice {
    return TrailerYardNotice(
        code = code,
        title = title,
        expiresAt = 0,
        attributes = mutableMapOf<String, String>(),
        pallets = 0,
        depot = "",
        status = TrailerYardNoticeStatus.CREATED,
    )
}
