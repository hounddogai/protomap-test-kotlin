package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.customs.clock.Clock

/** Moves regional declaration quotas through their life cycle and keeps a journal of what it did. */
data class RegionalDeclarationQuotaDesk(
    var store: RegionalDeclarationQuotaStore,
    var validator: RegionalDeclarationQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalDeclarationQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional declaration quota $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalDeclarationQuotaStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional declaration quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalDeclarationQuotaStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional declaration quota $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRegionalDeclarationQuotaDesk(store: RegionalDeclarationQuotaStore, clock: Clock): RegionalDeclarationQuotaDesk {
    return RegionalDeclarationQuotaDesk(
        store = store,
        validator = defaultRegionalDeclarationQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalDeclarationQuotaEngine(),
    )
}
