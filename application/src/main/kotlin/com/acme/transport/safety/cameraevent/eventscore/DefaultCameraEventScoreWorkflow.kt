package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.safety.clock.Clock

/** Moves default camera event scores through their life cycle and keeps a journal of what it did. */
data class DefaultCameraEventScoreDesk(
    var store: DefaultCameraEventScoreStore,
    var validator: DefaultCameraEventScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default camera event score $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCameraEventScoreStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default camera event score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCameraEventScoreStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newDefaultCameraEventScoreDesk(store: DefaultCameraEventScoreStore, clock: Clock): DefaultCameraEventScoreDesk {
    return DefaultCameraEventScoreDesk(
        store = store,
        validator = defaultDefaultCameraEventScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
