package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves default locker digests through their life cycle and keeps a journal of what it did. */
data class DefaultLockerDigestService(
    var store: DefaultLockerDigestStore,
    var validator: DefaultLockerDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultLockerDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default locker digest $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultLockerDigestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default locker digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultLockerDigestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default locker digest $code not found")
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

fun newDefaultLockerDigestService(store: DefaultLockerDigestStore, clock: Clock, flags: Registry): DefaultLockerDigestService {
    return DefaultLockerDigestService(
        store = store,
        validator = defaultDefaultLockerDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultLockerDigestEngine(flags),
    )
}
