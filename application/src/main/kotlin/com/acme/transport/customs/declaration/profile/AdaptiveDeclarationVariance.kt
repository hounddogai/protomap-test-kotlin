package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive declaration variance is in its life cycle. */
enum class AdaptiveDeclarationVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive declaration variance. Its code is unique within a tenant. */
data class AdaptiveDeclarationVariance(
    var code: String,
    var name: String,
    var revision: Int,
    var expiresAt: Int,
    var phone: String,
    var tags: MutableList<String>,
    var active: Boolean,
    var stopCount: Int,
    var graceDays: Int,
    var discount: Double,
    var locale: String,
    var status: AdaptiveDeclarationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDeclarationVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDeclarationVarianceStatus): Boolean {
        when (status) {
            AdaptiveDeclarationVarianceStatus.IDLE -> {
                when (next) {
                    AdaptiveDeclarationVarianceStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveDeclarationVarianceStatus.WARMING -> {
                when (next) {
                    AdaptiveDeclarationVarianceStatus.READY -> return true
                }
                return false
            }
            AdaptiveDeclarationVarianceStatus.READY -> {
                when (next) {
                    AdaptiveDeclarationVarianceStatus.DRAINING, AdaptiveDeclarationVarianceStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveDeclarationVarianceStatus.DRAINING -> return next == AdaptiveDeclarationVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveDeclarationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("adaptive declaration variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((expiresAt.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveDeclarationVariance {
        return AdaptiveDeclarationVariance(
            code = code,
            name = name,
            revision = revision,
            expiresAt = expiresAt,
            phone = phone,
            tags = tags,
            active = active,
            stopCount = stopCount,
            graceDays = graceDays,
            discount = discount,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive declaration variance in status idle. */
fun newAdaptiveDeclarationVariance(code: String, name: String): AdaptiveDeclarationVariance {
    return AdaptiveDeclarationVariance(
        code = code,
        name = name,
        revision = 0,
        expiresAt = 0,
        phone = "",
        tags = mutableListOf<String>(),
        active = false,
        stopCount = 0,
        graceDays = 0,
        discount = 0.0,
        locale = "",
        status = AdaptiveDeclarationVarianceStatus.IDLE,
    )
}
