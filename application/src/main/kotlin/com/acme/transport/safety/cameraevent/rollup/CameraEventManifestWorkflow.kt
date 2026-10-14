package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.safety.clock.Clock

/** Moves camera event manifests through their life cycle and keeps a journal of what it did. */
data class CameraEventManifestWorkflow(
    var store: CameraEventManifestStore,
    var validator: CameraEventManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("camera event manifest $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("camera event manifest $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newCameraEventManifestWorkflow(store: CameraEventManifestStore, clock: Clock): CameraEventManifestWorkflow {
    return CameraEventManifestWorkflow(
        store = store,
        validator = defaultCameraEventManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventManifestEngine(),
    )
}
