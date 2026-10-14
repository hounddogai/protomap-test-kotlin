package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection bundle is in its life cycle. */
enum class InspectionBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection bundle. Its code is unique within a tenant. */
data class InspectionBundle(
    var code: String,
    var title: String,
    var phone: String,
    var expiresAt: Int,
    var windowDays: Int,
    var category: String,
    var summary: String,
    var status: InspectionBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionBundleStatus): Boolean {
        when (status) {
            InspectionBundleStatus.DRAFT -> {
                when (next) {
                    InspectionBundleStatus.ACTIVE -> return true
                }
                return false
            }
            InspectionBundleStatus.PENDING -> {
                when (next) {
                    InspectionBundleStatus.CLOSED -> return true
                }
                return false
            }
            InspectionBundleStatus.ACTIVE -> return next == InspectionBundleStatus.SUSPENDED || next == InspectionBundleStatus.CLOSED
            InspectionBundleStatus.SUSPENDED -> {
                when (next) {
                    InspectionBundleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionBundleStatus) {
        if (!canMoveTo(next)) {
            error("inspection bundle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round((expiresAt.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): InspectionBundle {
        return InspectionBundle(
            code = code,
            title = title,
            phone = phone,
            expiresAt = expiresAt,
            windowDays = windowDays,
            category = category,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a inspection bundle in status draft. */
fun newInspectionBundle(code: String, title: String): InspectionBundle {
    return InspectionBundle(
        code = code,
        title = title,
        phone = "",
        expiresAt = 0,
        windowDays = 0,
        category = "",
        summary = "",
        status = InspectionBundleStatus.DRAFT,
    )
}
