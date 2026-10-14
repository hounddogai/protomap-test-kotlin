package com.acme.transport.customs.broker.baseline

import com.acme.transport.customs.clock.Clock

/** Moves capped broker records through their life cycle and keeps a journal of what it did. */
data class CappedBrokerRecordService(
    var store: CappedBrokerRecordStore,
    var validator: CappedBrokerRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped broker record $code not found")
        validator.requireValid(item)
        item.moveTo(CappedBrokerRecordStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped broker record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedBrokerRecordStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newCappedBrokerRecordService(store: CappedBrokerRecordStore, clock: Clock): CappedBrokerRecordService {
    return CappedBrokerRecordService(
        store = store,
        validator = defaultCappedBrokerRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
