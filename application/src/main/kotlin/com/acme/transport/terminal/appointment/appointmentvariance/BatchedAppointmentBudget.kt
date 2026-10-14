package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.text.normalize

/** Where a batched appointment budget is in its life cycle. */
enum class BatchedAppointmentBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched appointment budget. Its code is unique within a tenant. */
data class BatchedAppointmentBudget(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var quantity: Int,
    var currency: String,
    var source: String,
    var status: BatchedAppointmentBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAppointmentBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAppointmentBudgetStatus): Boolean {
        when (status) {
            BatchedAppointmentBudgetStatus.IDLE -> {
                when (next) {
                    BatchedAppointmentBudgetStatus.READY -> return true
                }
                return false
            }
            BatchedAppointmentBudgetStatus.WARMING -> {
                when (next) {
                    BatchedAppointmentBudgetStatus.DRAINING -> return true
                }
                return false
            }
            BatchedAppointmentBudgetStatus.READY -> return next == BatchedAppointmentBudgetStatus.DRAINING || next == BatchedAppointmentBudgetStatus.STOPPED
            BatchedAppointmentBudgetStatus.DRAINING -> {
                when (next) {
                    BatchedAppointmentBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAppointmentBudgetStatus) {
        if (!canMoveTo(next)) {
            error("batched appointment budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): BatchedAppointmentBudget {
        return BatchedAppointmentBudget(
            code = code,
            title = title,
            attributes = attributes,
            quantity = quantity,
            currency = currency,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched appointment budget in status idle. */
fun newBatchedAppointmentBudget(code: String, title: String): BatchedAppointmentBudget {
    return BatchedAppointmentBudget(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        quantity = 0,
        currency = "",
        source = "",
        status = BatchedAppointmentBudgetStatus.IDLE,
    )
}
