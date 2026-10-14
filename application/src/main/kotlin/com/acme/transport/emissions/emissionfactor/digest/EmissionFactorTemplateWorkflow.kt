package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves emission factor templates through their life cycle and keeps a journal of what it did. */
data class EmissionFactorTemplateDesk(
    var store: EmissionFactorTemplateStore,
    var validator: EmissionFactorTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("emission factor template $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorTemplateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorTemplateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("emission factor template $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newEmissionFactorTemplateDesk(store: EmissionFactorTemplateStore, clock: Clock, flags: Registry): EmissionFactorTemplateDesk {
    return EmissionFactorTemplateDesk(
        store = store,
        validator = defaultEmissionFactorTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorTemplateEngine(flags),
    )
}
