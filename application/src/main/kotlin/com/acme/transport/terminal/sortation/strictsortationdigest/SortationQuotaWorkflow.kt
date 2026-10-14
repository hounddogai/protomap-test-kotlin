package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation quotas through their life cycle and keeps a journal of what it did. */
data class SortationQuotaService(
    var store: SortationQuotaStore,
    var validator: SortationQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("sortation quota $code not found")
        validator.requireValid(item)
        item.moveTo(SortationQuotaStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationQuotaStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("sortation quota $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newSortationQuotaService(store: SortationQuotaStore, clock: Clock, flags: Registry): SortationQuotaService {
    return SortationQuotaService(
        store = store,
        validator = defaultSortationQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationQuotaEngine(flags),
    )
}
