package com.acme.transport.network.servicearea.budget

import com.acme.transport.network.clock.Clock

/** Moves default service area batches through their life cycle and keeps a journal of what it did. */
data class DefaultServiceAreaBatchDesk(
    var store: DefaultServiceAreaBatchStore,
    var validator: DefaultServiceAreaBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default service area batch $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultServiceAreaBatchStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default service area batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultServiceAreaBatchStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newDefaultServiceAreaBatchDesk(store: DefaultServiceAreaBatchStore, clock: Clock): DefaultServiceAreaBatchDesk {
    return DefaultServiceAreaBatchDesk(
        store = store,
        validator = defaultDefaultServiceAreaBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
