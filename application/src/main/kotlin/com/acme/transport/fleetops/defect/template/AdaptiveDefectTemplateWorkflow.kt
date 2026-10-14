package com.acme.transport.fleetops.defect.template

import com.acme.transport.fleetops.clock.Clock

/** Moves adaptive defect templates through their life cycle and keeps a journal of what it did. */
data class AdaptiveDefectTemplateCoordinator(
    var store: AdaptiveDefectTemplateStore,
    var validator: AdaptiveDefectTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveDefectTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive defect template $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDefectTemplateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive defect template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDefectTemplateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive defect template $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newAdaptiveDefectTemplateCoordinator(store: AdaptiveDefectTemplateStore, clock: Clock): AdaptiveDefectTemplateCoordinator {
    return AdaptiveDefectTemplateCoordinator(
        store = store,
        validator = defaultAdaptiveDefectTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveDefectTemplateEngine(),
    )
}
