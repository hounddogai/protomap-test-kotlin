package com.acme.transport.customs.declaration.forecast

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default declaration mappings through their life cycle and keeps a journal of what it did. */
data class DefaultDeclarationMappingService(
    var store: DefaultDeclarationMappingStore,
    var validator: DefaultDeclarationMappingValidatorExtra,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultDeclarationMappingEngineExtra,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default declaration mapping $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultDeclarationMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default declaration mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultDeclarationMappingStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default declaration mapping $code not found")
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

fun newDefaultDeclarationMappingService(store: DefaultDeclarationMappingStore, clock: Clock, flags: Registry): DefaultDeclarationMappingService {
    return DefaultDeclarationMappingService(
        store = store,
        validator = defaultDefaultDeclarationMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultDeclarationMappingEngine(flags),
    )
}
