package com.acme.transport.linehaul.tender.batch

import com.acme.transport.linehaul.clock.Clock

/** Moves tender preferences through their life cycle and keeps a journal of what it did. */
data class TenderPreferenceWorkflow(
    var store: TenderPreferenceStore,
    var validator: TenderPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tender preference $code not found")
        validator.requireValid(item)
        item.moveTo(TenderPreferenceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tender preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderPreferenceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newTenderPreferenceWorkflow(store: TenderPreferenceStore, clock: Clock): TenderPreferenceWorkflow {
    return TenderPreferenceWorkflow(
        store = store,
        validator = defaultTenderPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
