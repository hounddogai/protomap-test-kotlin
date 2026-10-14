package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.crewing.clock.Clock

/**
 * Moves batched duty period agreements through their life cycle and keeps a journal of what it did.
 */
data class BatchedDutyPeriodAgreementDesk(
    var store: BatchedDutyPeriodAgreementStore,
    var validator: BatchedDutyPeriodAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedDutyPeriodAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched duty period agreement $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDutyPeriodAgreementStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched duty period agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDutyPeriodAgreementStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched duty period agreement $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            val problems = validator.validate(item)
            if (problems.size == 0) {
                continue
            }
            note("${item.code} needs attention: ${problems.joinToString(", ")}")
            changed += 1
        }
        return changed
    }
}

fun newBatchedDutyPeriodAgreementDesk(store: BatchedDutyPeriodAgreementStore, clock: Clock): BatchedDutyPeriodAgreementDesk {
    return BatchedDutyPeriodAgreementDesk(
        store = store,
        validator = defaultBatchedDutyPeriodAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedDutyPeriodAgreementEngine(),
    )
}
