package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves capped attempt records through their life cycle and keeps a journal of what it did. */
data class CappedAttemptRecordService(
    var store: CappedAttemptRecordStore,
    var validator: CappedAttemptRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedAttemptRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped attempt record $code not found")
        validator.requireValid(item)
        item.moveTo(CappedAttemptRecordStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped attempt record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedAttemptRecordStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped attempt record $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newCappedAttemptRecordService(store: CappedAttemptRecordStore, clock: Clock, flags: Registry): CappedAttemptRecordService {
    return CappedAttemptRecordService(
        store = store,
        validator = defaultCappedAttemptRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedAttemptRecordEngine(flags),
    )
}
