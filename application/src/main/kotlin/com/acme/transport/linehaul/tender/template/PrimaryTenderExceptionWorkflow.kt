package com.acme.transport.linehaul.tender.template

import com.acme.transport.linehaul.clock.Clock

/** Moves primary tender exceptions through their life cycle and keeps a journal of what it did. */
data class PrimaryTenderExceptionManager(
    var store: PrimaryTenderExceptionStore,
    var validator: PrimaryTenderExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary tender exception $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTenderExceptionStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary tender exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTenderExceptionStatus.EXPIRED)
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

fun newPrimaryTenderExceptionManager(store: PrimaryTenderExceptionStore, clock: Clock): PrimaryTenderExceptionManager {
    return PrimaryTenderExceptionManager(
        store = store,
        validator = defaultPrimaryTenderExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
