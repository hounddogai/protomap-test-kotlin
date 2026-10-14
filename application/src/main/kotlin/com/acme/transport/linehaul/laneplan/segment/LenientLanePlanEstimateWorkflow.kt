package com.acme.transport.linehaul.laneplan.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves lenient lane plan estimates through their life cycle and keeps a journal of what it did. */
data class LenientLanePlanEstimateCoordinator(
    var store: LenientLanePlanEstimateStore,
    var validator: LenientLanePlanEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientLanePlanEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient lane plan estimate $code not found")
        validator.requireValid(item)
        item.moveTo(LenientLanePlanEstimateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient lane plan estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientLanePlanEstimateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient lane plan estimate $code not found")
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

fun newLenientLanePlanEstimateCoordinator(store: LenientLanePlanEstimateStore, clock: Clock, flags: Registry): LenientLanePlanEstimateCoordinator {
    return LenientLanePlanEstimateCoordinator(
        store = store,
        validator = defaultLenientLanePlanEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientLanePlanEstimateEngine(flags),
    )
}
