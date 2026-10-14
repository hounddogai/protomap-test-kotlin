package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.text.normalize

/** Where a parcel checkpoint is in its life cycle. */
enum class ParcelCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A parcel checkpoint. Its code is unique within a tenant. */
data class ParcelCheckpoint(
    var code: String,
    var name: String,
    var ceiling: Double,
    var visible: Boolean,
    var archived: Boolean,
    var depot: String,
    var dock: String,
    var status: ParcelCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelCheckpointStatus): Boolean {
        when (status) {
            ParcelCheckpointStatus.NEW -> {
                when (next) {
                    ParcelCheckpointStatus.QUEUED, ParcelCheckpointStatus.RUNNING -> return true
                }
                return false
            }
            ParcelCheckpointStatus.QUEUED -> {
                when (next) {
                    ParcelCheckpointStatus.DONE -> return true
                }
                return false
            }
            ParcelCheckpointStatus.RUNNING -> return next == ParcelCheckpointStatus.DONE || next == ParcelCheckpointStatus.FAILED
            ParcelCheckpointStatus.DONE -> return next == ParcelCheckpointStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ParcelCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("parcel checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = ceiling

    fun snapshot(): ParcelCheckpoint {
        return ParcelCheckpoint(
            code = code,
            name = name,
            ceiling = ceiling,
            visible = visible,
            archived = archived,
            depot = depot,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a parcel checkpoint in status new. */
fun newParcelCheckpoint(code: String, name: String): ParcelCheckpoint {
    return ParcelCheckpoint(
        code = code,
        name = name,
        ceiling = 0.0,
        visible = false,
        archived = false,
        depot = "",
        dock = "",
        status = ParcelCheckpointStatus.NEW,
    )
}
