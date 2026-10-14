package com.acme.transport.lastmile.drop.rule

import com.acme.transport.lastmile.clock.Clock

/** Moves strict drop groups through their life cycle and keeps a journal of what it did. */
data class StrictDropGroupManager(
    var store: StrictDropGroupStore,
    var validator: StrictDropGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict drop group $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDropGroupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict drop group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDropGroupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newStrictDropGroupManager(store: StrictDropGroupStore, clock: Clock): StrictDropGroupManager {
    return StrictDropGroupManager(
        store = store,
        validator = defaultStrictDropGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
