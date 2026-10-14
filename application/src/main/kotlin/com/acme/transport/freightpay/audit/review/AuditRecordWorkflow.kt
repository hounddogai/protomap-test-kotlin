package com.acme.transport.freightpay.audit.review

import com.acme.transport.freightpay.clock.Clock

/** Moves audit records through their life cycle and keeps a journal of what it did. */
data class AuditRecordService(
    var store: AuditRecordStore,
    var validator: AuditRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AuditRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("audit record $code not found")
        validator.requireValid(item)
        item.moveTo(AuditRecordStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("audit record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditRecordStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("audit record $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newAuditRecordService(store: AuditRecordStore, clock: Clock): AuditRecordService {
    return AuditRecordService(
        store = store,
        validator = defaultAuditRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAuditRecordEngine(),
    )
}
