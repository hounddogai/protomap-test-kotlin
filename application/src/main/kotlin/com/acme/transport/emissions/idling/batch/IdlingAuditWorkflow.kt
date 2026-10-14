package com.acme.transport.emissions.idling.batch

import com.acme.transport.emissions.clock.Clock

/** Moves idling audits through their life cycle and keeps a journal of what it did. */
data class IdlingAuditService(
    var store: IdlingAuditStore,
    var validator: IdlingAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("idling audit $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingAuditStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("idling audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingAuditStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("idling audit $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newIdlingAuditService(store: IdlingAuditStore, clock: Clock): IdlingAuditService {
    return IdlingAuditService(
        store = store,
        validator = defaultIdlingAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingAuditEngine(),
    )
}
