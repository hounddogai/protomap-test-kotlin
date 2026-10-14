package com.acme.transport.customs.inspection.budget

import com.acme.transport.customs.clock.Clock

/** Moves primary inspection audits through their life cycle and keeps a journal of what it did. */
data class PrimaryInspectionAuditService(
    var store: PrimaryInspectionAuditStore,
    var validator: PrimaryInspectionAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryInspectionAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary inspection audit $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryInspectionAuditStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary inspection audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryInspectionAuditStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary inspection audit $code not found")
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

fun newPrimaryInspectionAuditService(store: PrimaryInspectionAuditStore, clock: Clock): PrimaryInspectionAuditService {
    return PrimaryInspectionAuditService(
        store = store,
        validator = defaultPrimaryInspectionAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryInspectionAuditEngine(),
    )
}
