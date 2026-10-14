package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.safety.clock.Clock

/** Moves lenient camera event policies through their life cycle and keeps a journal of what it did. */
data class LenientCameraEventPolicyCoordinator(
    var store: LenientCameraEventPolicyStore,
    var validator: LenientCameraEventPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient camera event policy $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCameraEventPolicyStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient camera event policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCameraEventPolicyStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newLenientCameraEventPolicyCoordinator(store: LenientCameraEventPolicyStore, clock: Clock): LenientCameraEventPolicyCoordinator {
    return LenientCameraEventPolicyCoordinator(
        store = store,
        validator = defaultLenientCameraEventPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
