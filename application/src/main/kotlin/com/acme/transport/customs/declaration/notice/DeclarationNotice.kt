package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration notice records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration notice is in its life cycle. */
enum class DeclarationNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a declaration notice and its current status. */
data class DeclarationNotice(
    var code: String,
    var title: String,
    var country: String,
    var windowDays: Int,
    var amount: Double,
    var notes: String,
    var carrier: String,
    var status: DeclarationNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationNoticeStatus): Boolean {
        when (status) {
            DeclarationNoticeStatus.NEW -> return next == DeclarationNoticeStatus.RUNNING || next == DeclarationNoticeStatus.FAILED
            DeclarationNoticeStatus.QUEUED -> return next == DeclarationNoticeStatus.DONE || next == DeclarationNoticeStatus.FAILED
            DeclarationNoticeStatus.RUNNING -> {
                when (next) {
                    DeclarationNoticeStatus.DONE, DeclarationNoticeStatus.FAILED -> return true
                }
                return false
            }
            DeclarationNoticeStatus.DONE -> {
                when (next) {
                    DeclarationNoticeStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationNoticeStatus) {
        if (!canMoveTo(next)) {
            error("declaration notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedVolume(): Double = round((amount + windowDays.toDouble()) * 100) / 100

    fun snapshot(): DeclarationNotice {
        return DeclarationNotice(
            code = code,
            title = title,
            country = country,
            windowDays = windowDays,
            amount = amount,
            notes = notes,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a declaration notice in status new. */
fun newDeclarationNotice(code: String, title: String): DeclarationNotice {
    return DeclarationNotice(
        code = code,
        title = title,
        country = "",
        windowDays = 0,
        amount = 0.0,
        notes = "",
        carrier = "",
        status = DeclarationNoticeStatus.NEW,
    )
}
