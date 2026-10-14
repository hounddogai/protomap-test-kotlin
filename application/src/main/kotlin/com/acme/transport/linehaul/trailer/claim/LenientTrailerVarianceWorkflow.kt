package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.linehaul.clock.Clock

/** Moves lenient trailer variances through their life cycle and keeps a journal of what it did. */
data class LenientTrailerVarianceDesk(
    var store: LenientTrailerVarianceStore,
    var validator: LenientTrailerVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientTrailerVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient trailer variance $code not found")
        validator.requireValid(item)
        item.moveTo(LenientTrailerVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient trailer variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientTrailerVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient trailer variance $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newLenientTrailerVarianceDesk(store: LenientTrailerVarianceStore, clock: Clock): LenientTrailerVarianceDesk {
    return LenientTrailerVarianceDesk(
        store = store,
        validator = defaultLenientTrailerVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientTrailerVarianceEngine(),
    )
}
