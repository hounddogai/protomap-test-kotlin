package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.lastmile.clock.Clock

/** Moves global parcel drafts through their life cycle and keeps a journal of what it did. */
data class GlobalParcelDraftService(
    var store: GlobalParcelDraftStore,
    var validator: GlobalParcelDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalParcelDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global parcel draft $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalParcelDraftStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global parcel draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalParcelDraftStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global parcel draft $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newGlobalParcelDraftService(store: GlobalParcelDraftStore, clock: Clock): GlobalParcelDraftService {
    return GlobalParcelDraftService(
        store = store,
        validator = defaultGlobalParcelDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalParcelDraftEngine(),
    )
}
