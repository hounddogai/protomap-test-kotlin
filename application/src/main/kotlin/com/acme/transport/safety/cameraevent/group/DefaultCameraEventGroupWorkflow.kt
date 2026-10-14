package com.acme.transport.safety.cameraevent.group

import com.acme.transport.safety.clock.Clock

/** Moves default camera event groups through their life cycle and keeps a journal of what it did. */
data class DefaultCameraEventGroupWorkflow(
    var store: DefaultCameraEventGroupStore,
    var validator: DefaultCameraEventGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultCameraEventGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default camera event group $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCameraEventGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default camera event group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCameraEventGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default camera event group $code not found")
        val result = engine.compute(item)
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

fun newDefaultCameraEventGroupWorkflow(store: DefaultCameraEventGroupStore, clock: Clock): DefaultCameraEventGroupWorkflow {
    return DefaultCameraEventGroupWorkflow(
        store = store,
        validator = defaultDefaultCameraEventGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultCameraEventGroupEngine(),
    )
}
