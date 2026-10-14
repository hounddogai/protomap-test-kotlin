package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves weighted accessorial templates through their life cycle and keeps a journal of what it did.
 */
data class WeightedAccessorialTemplateCoordinator(
    var store: WeightedAccessorialTemplateStore,
    var validator: WeightedAccessorialTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedAccessorialTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted accessorial template $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedAccessorialTemplateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted accessorial template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedAccessorialTemplateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted accessorial template $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newWeightedAccessorialTemplateCoordinator(store: WeightedAccessorialTemplateStore, clock: Clock): WeightedAccessorialTemplateCoordinator {
    return WeightedAccessorialTemplateCoordinator(
        store = store,
        validator = defaultWeightedAccessorialTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedAccessorialTemplateEngine(),
    )
}
