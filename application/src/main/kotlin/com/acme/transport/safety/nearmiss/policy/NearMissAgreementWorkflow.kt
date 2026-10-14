package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.safety.clock.Clock

/** Moves near miss agreements through their life cycle and keeps a journal of what it did. */
data class NearMissAgreementManager(
    var store: NearMissAgreementStore,
    var validator: NearMissAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NearMissAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("near miss agreement $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissAgreementStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissAgreementStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("near miss agreement $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newNearMissAgreementManager(store: NearMissAgreementStore, clock: Clock): NearMissAgreementManager {
    return NearMissAgreementManager(
        store = store,
        validator = defaultNearMissAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNearMissAgreementEngine(),
    )
}
