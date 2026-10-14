package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.freightpay.clock.Clock

/** Moves accessorial groups through their life cycle and keeps a journal of what it did. */
data class AccessorialGroupManager(
    var store: AccessorialGroupStore,
    var validator: AccessorialGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AccessorialGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("accessorial group $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialGroupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialGroupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("accessorial group $code not found")
        val result = engine.run(item)
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

fun newAccessorialGroupManager(store: AccessorialGroupStore, clock: Clock): AccessorialGroupManager {
    return AccessorialGroupManager(
        store = store,
        validator = defaultAccessorialGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAccessorialGroupEngine(),
    )
}
