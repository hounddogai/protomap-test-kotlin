package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling tariff checkpoint is in its life cycle. */
enum class RollingTariffCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A rolling tariff checkpoint as the tariff team keeps it. */
data class RollingTariffCheckpoint(
    var code: String,
    var title: String,
    var region: String,
    var source: String,
    var visible: Boolean,
    var limit: Int,
    var country: String,
    var lane: String,
    var retries: Int,
    var dock: String,
    var status: RollingTariffCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTariffCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTariffCheckpointStatus): Boolean {
        when (status) {
            RollingTariffCheckpointStatus.OPEN -> return next == RollingTariffCheckpointStatus.IN_REVIEW || next == RollingTariffCheckpointStatus.RESOLVED
            RollingTariffCheckpointStatus.IN_REVIEW -> {
                when (next) {
                    RollingTariffCheckpointStatus.REOPENED -> return true
                }
                return false
            }
            RollingTariffCheckpointStatus.RESOLVED -> {
                when (next) {
                    RollingTariffCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingTariffCheckpointStatus.REOPENED -> return next == RollingTariffCheckpointStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingTariffCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("rolling tariff checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netExposure(): Double = round((limit.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): RollingTariffCheckpoint {
        return RollingTariffCheckpoint(
            code = code,
            title = title,
            region = region,
            source = source,
            visible = visible,
            limit = limit,
            country = country,
            lane = lane,
            retries = retries,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling tariff checkpoint in status open. */
fun newRollingTariffCheckpoint(code: String, title: String): RollingTariffCheckpoint {
    return RollingTariffCheckpoint(
        code = code,
        title = title,
        region = "",
        source = "",
        visible = false,
        limit = 0,
        country = "",
        lane = "",
        retries = 0,
        dock = "",
        status = RollingTariffCheckpointStatus.OPEN,
    )
}
