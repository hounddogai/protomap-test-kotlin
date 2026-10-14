package com.acme.transport.linehaul.tender.profile

import com.acme.transport.linehaul.clock.Clock

/** Moves tender exceptions through their life cycle and keeps a journal of what it did. */
data class TenderExceptionService(
    var store: TenderExceptionStore,
    var validator: TenderExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tender exception $code not found")
        validator.requireValid(item)
        item.moveTo(TenderExceptionStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tender exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderExceptionStatus.STOPPED)
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

fun newTenderExceptionService(store: TenderExceptionStore, clock: Clock): TenderExceptionService {
    return TenderExceptionService(
        store = store,
        validator = defaultTenderExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
