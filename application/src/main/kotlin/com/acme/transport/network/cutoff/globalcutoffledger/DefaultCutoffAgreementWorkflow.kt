package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.network.clock.Clock

/** Moves default cutoff agreements through their life cycle and keeps a journal of what it did. */
data class DefaultCutoffAgreementWorkflow(
    var store: DefaultCutoffAgreementStore,
    var validator: DefaultCutoffAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default cutoff agreement $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCutoffAgreementStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default cutoff agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCutoffAgreementStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newDefaultCutoffAgreementWorkflow(store: DefaultCutoffAgreementStore, clock: Clock): DefaultCutoffAgreementWorkflow {
    return DefaultCutoffAgreementWorkflow(
        store = store,
        validator = defaultDefaultCutoffAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
