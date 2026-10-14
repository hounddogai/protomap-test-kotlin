package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.network.clock.Clock

/** Moves rolling cutoff audits through their life cycle and keeps a journal of what it did. */
data class RollingCutoffAuditWorkflow(
    var store: RollingCutoffAuditStore,
    var validator: RollingCutoffAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling cutoff audit $code not found")
        validator.requireValid(item)
        item.moveTo(RollingCutoffAuditStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling cutoff audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingCutoffAuditStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newRollingCutoffAuditWorkflow(store: RollingCutoffAuditStore, clock: Clock): RollingCutoffAuditWorkflow {
    return RollingCutoffAuditWorkflow(
        store = store,
        validator = defaultRollingCutoffAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
