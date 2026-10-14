package com.acme.transport.freightpay.dispute.recorditem

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback dispute records through their life cycle and keeps a journal of what it did. */
data class FallbackDisputeRecordService(
    var store: FallbackDisputeRecordStore,
    var validator: FallbackDisputeRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackDisputeRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback dispute record $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDisputeRecordStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback dispute record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDisputeRecordStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback dispute record $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newFallbackDisputeRecordService(store: FallbackDisputeRecordStore, clock: Clock, flags: Registry): FallbackDisputeRecordService {
    return FallbackDisputeRecordService(
        store = store,
        validator = defaultFallbackDisputeRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackDisputeRecordEngine(flags),
    )
}
