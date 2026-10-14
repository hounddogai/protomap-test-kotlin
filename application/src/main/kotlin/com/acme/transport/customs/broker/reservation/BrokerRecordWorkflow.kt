package com.acme.transport.customs.broker.reservation

import com.acme.transport.customs.clock.Clock

/** Moves broker records through their life cycle and keeps a journal of what it did. */
data class BrokerRecordManager(
    var store: BrokerRecordStore,
    var validator: BrokerRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("broker record $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerRecordStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("broker record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerRecordStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newBrokerRecordManager(store: BrokerRecordStore, clock: Clock): BrokerRecordManager {
    return BrokerRecordManager(
        store = store,
        validator = defaultBrokerRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
