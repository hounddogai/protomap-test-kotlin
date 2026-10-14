package com.acme.transport.crewing.qualification.rule

import com.acme.transport.crewing.clock.Clock

/** Moves rolling qualification rules through their life cycle and keeps a journal of what it did. */
data class RollingQualificationRuleWorkflow(
    var store: RollingQualificationRuleStore,
    var validator: RollingQualificationRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling qualification rule $code not found")
        validator.requireValid(item)
        item.moveTo(RollingQualificationRuleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling qualification rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingQualificationRuleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newRollingQualificationRuleWorkflow(store: RollingQualificationRuleStore, clock: Clock): RollingQualificationRuleWorkflow {
    return RollingQualificationRuleWorkflow(
        store = store,
        validator = defaultRollingQualificationRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
