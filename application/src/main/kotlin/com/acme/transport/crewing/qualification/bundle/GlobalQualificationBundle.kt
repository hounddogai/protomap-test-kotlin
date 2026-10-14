package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global qualification bundle records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a global qualification bundle is in its life cycle. */
enum class GlobalQualificationBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global qualification bundle as the qualification team keeps it. */
data class GlobalQualificationBundle(
    var code: String,
    var name: String,
    var externalRef: String,
    var title: String,
    var summary: String,
    var batchSize: Int,
    var archived: Boolean,
    var floorValue: Double,
    var status: GlobalQualificationBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalQualificationBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalQualificationBundleStatus): Boolean {
        when (status) {
            GlobalQualificationBundleStatus.DRAFT -> return next == GlobalQualificationBundleStatus.CLOSED
            GlobalQualificationBundleStatus.PENDING -> {
                when (next) {
                    GlobalQualificationBundleStatus.CLOSED -> return true
                }
                return false
            }
            GlobalQualificationBundleStatus.ACTIVE -> {
                when (next) {
                    GlobalQualificationBundleStatus.SUSPENDED, GlobalQualificationBundleStatus.CLOSED -> return true
                }
                return false
            }
            GlobalQualificationBundleStatus.SUSPENDED -> return next == GlobalQualificationBundleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: GlobalQualificationBundleStatus) {
        if (!canMoveTo(next)) {
            error("global qualification bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round((floorValue + batchSize.toDouble()) * 100) / 100

    fun snapshot(): GlobalQualificationBundle {
        return GlobalQualificationBundle(
            code = code,
            name = name,
            externalRef = externalRef,
            title = title,
            summary = summary,
            batchSize = batchSize,
            archived = archived,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global qualification bundle in status draft. */
fun newGlobalQualificationBundle(code: String, name: String): GlobalQualificationBundle {
    return GlobalQualificationBundle(
        code = code,
        name = name,
        externalRef = "",
        title = "",
        summary = "",
        batchSize = 0,
        archived = false,
        floorValue = 0.0,
        status = GlobalQualificationBundleStatus.DRAFT,
    )
}
