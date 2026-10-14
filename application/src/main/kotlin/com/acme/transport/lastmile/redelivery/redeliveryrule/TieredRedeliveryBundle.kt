package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered redelivery bundle is in its life cycle. */
enum class TieredRedeliveryBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered redelivery bundle. Its code is unique within a tenant. */
data class TieredRedeliveryBundle(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var ownerId: String,
    var limit: Int,
    var revision: Int,
    var status: TieredRedeliveryBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRedeliveryBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRedeliveryBundleStatus): Boolean {
        when (status) {
            TieredRedeliveryBundleStatus.NEW -> return next == TieredRedeliveryBundleStatus.QUEUED || next == TieredRedeliveryBundleStatus.RUNNING
            TieredRedeliveryBundleStatus.QUEUED -> return next == TieredRedeliveryBundleStatus.RUNNING || next == TieredRedeliveryBundleStatus.FAILED
            TieredRedeliveryBundleStatus.RUNNING -> return next == TieredRedeliveryBundleStatus.DONE || next == TieredRedeliveryBundleStatus.FAILED
            TieredRedeliveryBundleStatus.DONE -> {
                when (next) {
                    TieredRedeliveryBundleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredRedeliveryBundleStatus) {
        if (!canMoveTo(next)) {
            error("tiered redelivery bundle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"tiered redelivery bundle"} $code"

    fun weightedLoad(): Double = round((limit.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): TieredRedeliveryBundle {
        return TieredRedeliveryBundle(
            code = code,
            name = name,
            distanceKm = distanceKm,
            ownerId = ownerId,
            limit = limit,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered redelivery bundle in status new. */
fun newTieredRedeliveryBundle(code: String, name: String): TieredRedeliveryBundle {
    return TieredRedeliveryBundle(
        code = code,
        name = name,
        distanceKm = 0.0,
        ownerId = "",
        limit = 0,
        revision = 0,
        status = TieredRedeliveryBundleStatus.NEW,
    )
}
