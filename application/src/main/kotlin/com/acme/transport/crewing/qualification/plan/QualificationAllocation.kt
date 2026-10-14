package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.text.normalize

/** Where a qualification allocation is in its life cycle. */
enum class QualificationAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A qualification allocation. Its code is unique within a tenant. */
data class QualificationAllocation(
    var code: String,
    var name: String,
    var label: String,
    var approved: Boolean,
    var checksum: String,
    var quantity: Int,
    var status: QualificationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationAllocationStatus): Boolean {
        when (status) {
            QualificationAllocationStatus.PROPOSED -> return next == QualificationAllocationStatus.REJECTED || next == QualificationAllocationStatus.WITHDRAWN
            QualificationAllocationStatus.APPROVED -> {
                when (next) {
                    QualificationAllocationStatus.REJECTED, QualificationAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
            QualificationAllocationStatus.REJECTED -> {
                when (next) {
                    QualificationAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("qualification allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): QualificationAllocation {
        return QualificationAllocation(
            code = code,
            name = name,
            label = label,
            approved = approved,
            checksum = checksum,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a qualification allocation in status proposed. */
fun newQualificationAllocation(code: String, name: String): QualificationAllocation {
    return QualificationAllocation(
        code = code,
        name = name,
        label = "",
        approved = false,
        checksum = "",
        quantity = 0,
        status = QualificationAllocationStatus.PROPOSED,
    )
}
