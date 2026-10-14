package com.acme.transport.customs.declaration.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default declaration review is in its life cycle. */
enum class DefaultDeclarationReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default declaration review as the declaration team keeps it. */
data class DefaultDeclarationReview(
    var code: String,
    var name: String,
    var version: Int,
    var rate: Double,
    var label: String,
    var updatedAt: Int,
    var endsAt: Int,
    var capacity: Int,
    var limit: Int,
    var category: String,
    var status: DefaultDeclarationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDeclarationReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDeclarationReviewStatus): Boolean {
        when (status) {
            DefaultDeclarationReviewStatus.NEW -> {
                when (next) {
                    DefaultDeclarationReviewStatus.DONE, DefaultDeclarationReviewStatus.FAILED -> return true
                }
                return false
            }
            DefaultDeclarationReviewStatus.QUEUED -> return next == DefaultDeclarationReviewStatus.RUNNING
            DefaultDeclarationReviewStatus.RUNNING -> {
                when (next) {
                    DefaultDeclarationReviewStatus.FAILED -> return true
                }
                return false
            }
            DefaultDeclarationReviewStatus.DONE -> {
                when (next) {
                    DefaultDeclarationReviewStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDeclarationReviewStatus) {
        if (!canMoveTo(next)) {
            error("default declaration review $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"default declaration review"} $code"

    fun adjustedExposure(): Double = round((endsAt.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): DefaultDeclarationReview {
        return DefaultDeclarationReview(
            code = code,
            name = name,
            version = version,
            rate = rate,
            label = label,
            updatedAt = updatedAt,
            endsAt = endsAt,
            capacity = capacity,
            limit = limit,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default declaration review in status new. */
fun newDefaultDeclarationReview(code: String, name: String): DefaultDeclarationReview {
    return DefaultDeclarationReview(
        code = code,
        name = name,
        version = 0,
        rate = 0.0,
        label = "",
        updatedAt = 0,
        endsAt = 0,
        capacity = 0,
        limit = 0,
        category = "",
        status = DefaultDeclarationReviewStatus.NEW,
    )
}
