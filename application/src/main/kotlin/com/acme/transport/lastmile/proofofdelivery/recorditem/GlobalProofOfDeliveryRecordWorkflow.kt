package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves global proof of delivery records through their life cycle and keeps a journal of what it did.
 */
data class GlobalProofOfDeliveryRecordService(
    var store: GlobalProofOfDeliveryRecordStore,
    var validator: GlobalProofOfDeliveryRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalProofOfDeliveryRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global proof of delivery record $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalProofOfDeliveryRecordStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global proof of delivery record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalProofOfDeliveryRecordStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global proof of delivery record $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newGlobalProofOfDeliveryRecordService(store: GlobalProofOfDeliveryRecordStore, clock: Clock, flags: Registry): GlobalProofOfDeliveryRecordService {
    return GlobalProofOfDeliveryRecordService(
        store = store,
        validator = defaultGlobalProofOfDeliveryRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalProofOfDeliveryRecordEngine(flags),
    )
}
