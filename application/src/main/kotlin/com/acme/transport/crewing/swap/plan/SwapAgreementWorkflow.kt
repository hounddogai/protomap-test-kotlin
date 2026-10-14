package com.acme.transport.crewing.swap.plan

import com.acme.transport.crewing.clock.Clock

/** Moves swap agreements through their life cycle and keeps a journal of what it did. */
data class SwapAgreementService(
    var store: SwapAgreementStore,
    var validator: SwapAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SwapAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("swap agreement $code not found")
        validator.requireValid(item)
        item.moveTo(SwapAgreementStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("swap agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapAgreementStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("swap agreement $code not found")
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

fun newSwapAgreementService(store: SwapAgreementStore, clock: Clock): SwapAgreementService {
    return SwapAgreementService(
        store = store,
        validator = defaultSwapAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSwapAgreementEngine(),
    )
}
