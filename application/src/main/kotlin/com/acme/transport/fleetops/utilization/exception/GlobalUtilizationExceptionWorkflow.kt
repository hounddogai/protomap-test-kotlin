package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global utilization exceptions through their life cycle and keeps a journal of what it did. */
data class GlobalUtilizationExceptionService(
    var store: GlobalUtilizationExceptionStore,
    var validator: GlobalUtilizationExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalUtilizationExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global utilization exception $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalUtilizationExceptionStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global utilization exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalUtilizationExceptionStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global utilization exception $code not found")
        val result = engine.price(item)
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

fun newGlobalUtilizationExceptionService(store: GlobalUtilizationExceptionStore, clock: Clock, flags: Registry): GlobalUtilizationExceptionService {
    return GlobalUtilizationExceptionService(
        store = store,
        validator = defaultGlobalUtilizationExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalUtilizationExceptionEngine(flags),
    )
}
