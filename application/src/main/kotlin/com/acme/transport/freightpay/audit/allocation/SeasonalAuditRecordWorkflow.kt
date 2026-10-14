package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.freightpay.clock.Clock

/** Moves seasonal audit records through their life cycle and keeps a journal of what it did. */
data class SeasonalAuditRecordService(
    var store: SeasonalAuditRecordStore,
    var validator: SeasonalAuditRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalAuditRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal audit record $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalAuditRecordStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal audit record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalAuditRecordStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal audit record $code not found")
        val result = engine.evaluate(item)
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

fun newSeasonalAuditRecordService(store: SeasonalAuditRecordStore, clock: Clock): SeasonalAuditRecordService {
    return SeasonalAuditRecordService(
        store = store,
        validator = defaultSeasonalAuditRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalAuditRecordEngine(),
    )
}
