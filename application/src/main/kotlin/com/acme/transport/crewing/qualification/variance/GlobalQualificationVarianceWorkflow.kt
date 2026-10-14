package com.acme.transport.crewing.qualification.variance

import com.acme.transport.crewing.clock.Clock

/**
 * Moves global qualification variances through their life cycle and keeps a journal of what it did.
 */
data class GlobalQualificationVarianceDesk(
    var store: GlobalQualificationVarianceStore,
    var validator: GlobalQualificationVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global qualification variance $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalQualificationVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global qualification variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalQualificationVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newGlobalQualificationVarianceDesk(store: GlobalQualificationVarianceStore, clock: Clock): GlobalQualificationVarianceDesk {
    return GlobalQualificationVarianceDesk(
        store = store,
        validator = defaultGlobalQualificationVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
