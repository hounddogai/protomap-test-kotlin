package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.freightpay.clock.Clock

/** Moves default accessorial scores through their life cycle and keeps a journal of what it did. */
data class DefaultAccessorialScoreManager(
    var store: DefaultAccessorialScoreStore,
    var validator: DefaultAccessorialScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default accessorial score $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultAccessorialScoreStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default accessorial score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultAccessorialScoreStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newDefaultAccessorialScoreManager(store: DefaultAccessorialScoreStore, clock: Clock): DefaultAccessorialScoreManager {
    return DefaultAccessorialScoreManager(
        store = store,
        validator = defaultDefaultAccessorialScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
