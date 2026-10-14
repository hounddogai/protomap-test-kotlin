package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.freightpay.clock.Clock

/** Moves audit cycles through their life cycle and keeps a journal of what it did. */
data class AuditCycleService(
    var store: AuditCycleStore,
    var validator: AuditCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AuditCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("audit cycle $code not found")
        validator.requireValid(item)
        item.moveTo(AuditCycleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("audit cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditCycleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("audit cycle $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newAuditCycleService(store: AuditCycleStore, clock: Clock): AuditCycleService {
    return AuditCycleService(
        store = store,
        validator = defaultAuditCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAuditCycleEngine(),
    )
}
