package com.acme.transport.customs.clearance.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default clearance notice is in its life cycle. */
enum class DefaultClearanceNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a default clearance notice and its current status. */
data class DefaultClearanceNotice(
    var code: String,
    var title: String,
    var carrier: String,
    var pallets: Int,
    var description: String,
    var dock: String,
    var amount: Double,
    var ceiling: Double,
    var category: String,
    var label: String,
    var name: String,
    var status: DefaultClearanceNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultClearanceNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultClearanceNoticeStatus): Boolean {
        when (status) {
            DefaultClearanceNoticeStatus.CREATED -> return next == DefaultClearanceNoticeStatus.PUBLISHED || next == DefaultClearanceNoticeStatus.RETIRED
            DefaultClearanceNoticeStatus.VALIDATED -> {
                when (next) {
                    DefaultClearanceNoticeStatus.RETIRED -> return true
                }
                return false
            }
            DefaultClearanceNoticeStatus.PUBLISHED -> {
                when (next) {
                    DefaultClearanceNoticeStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultClearanceNoticeStatus) {
        if (!canMoveTo(next)) {
            error("default clearance notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun netExposure(): Double = round((amount + ceiling) * 100) / 100

    fun snapshot(): DefaultClearanceNotice {
        return DefaultClearanceNotice(
            code = code,
            title = title,
            carrier = carrier,
            pallets = pallets,
            description = description,
            dock = dock,
            amount = amount,
            ceiling = ceiling,
            category = category,
            label = label,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default clearance notice in status created. */
fun newDefaultClearanceNotice(code: String, title: String): DefaultClearanceNotice {
    return DefaultClearanceNotice(
        code = code,
        title = title,
        carrier = "",
        pallets = 0,
        description = "",
        dock = "",
        amount = 0.0,
        ceiling = 0.0,
        category = "",
        label = "",
        name = "",
        status = DefaultClearanceNoticeStatus.CREATED,
    )
}
