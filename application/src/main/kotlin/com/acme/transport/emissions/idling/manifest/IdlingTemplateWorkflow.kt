package com.acme.transport.emissions.idling.manifest

import com.acme.transport.emissions.clock.Clock

/** Moves idling templates through their life cycle and keeps a journal of what it did. */
data class IdlingTemplateCoordinator(
    var store: IdlingTemplateStore,
    var validator: IdlingTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("idling template $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingTemplateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("idling template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingTemplateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newIdlingTemplateCoordinator(store: IdlingTemplateStore, clock: Clock): IdlingTemplateCoordinator {
    return IdlingTemplateCoordinator(
        store = store,
        validator = defaultIdlingTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
