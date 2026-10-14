package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.lastmile.clock.Clock

/** Moves tiered redelivery overrides through their life cycle and keeps a journal of what it did. */
data class TieredRedeliveryOverrideWorkflow(
    var store: TieredRedeliveryOverrideStore,
    var validator: TieredRedeliveryOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered redelivery override $code not found")
        validator.requireValid(item)
        item.moveTo(TieredRedeliveryOverrideStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered redelivery override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredRedeliveryOverrideStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newTieredRedeliveryOverrideWorkflow(store: TieredRedeliveryOverrideStore, clock: Clock): TieredRedeliveryOverrideWorkflow {
    return TieredRedeliveryOverrideWorkflow(
        store = store,
        validator = defaultTieredRedeliveryOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
