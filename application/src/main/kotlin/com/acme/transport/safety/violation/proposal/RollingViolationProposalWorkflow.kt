package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves rolling violation proposals through their life cycle and keeps a journal of what it did. */
data class RollingViolationProposalWorkflow(
    var store: RollingViolationProposalStore,
    var validator: RollingViolationProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingViolationProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling violation proposal $code not found")
        validator.requireValid(item)
        item.moveTo(RollingViolationProposalStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling violation proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingViolationProposalStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling violation proposal $code not found")
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

fun newRollingViolationProposalWorkflow(store: RollingViolationProposalStore, clock: Clock, flags: Registry): RollingViolationProposalWorkflow {
    return RollingViolationProposalWorkflow(
        store = store,
        validator = defaultRollingViolationProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingViolationProposalEngine(flags),
    )
}
