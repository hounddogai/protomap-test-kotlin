package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.lastmile.clock.Clock

/** Moves global drop exceptions through their life cycle and keeps a journal of what it did. */
data class GlobalDropExceptionService(
    var store: GlobalDropExceptionStore,
    var validator: GlobalDropExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global drop exception $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDropExceptionStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global drop exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDropExceptionStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newGlobalDropExceptionService(store: GlobalDropExceptionStore, clock: Clock): GlobalDropExceptionService {
    return GlobalDropExceptionService(
        store = store,
        validator = defaultGlobalDropExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
