package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.safety.clock.Clock

/** Moves camera event budgets through their life cycle and keeps a journal of what it did. */
data class CameraEventBudgetService(
    var store: CameraEventBudgetStore,
    var validator: CameraEventBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("camera event budget $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newCameraEventBudgetService(store: CameraEventBudgetStore, clock: Clock): CameraEventBudgetService {
    return CameraEventBudgetService(
        store = store,
        validator = defaultCameraEventBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
