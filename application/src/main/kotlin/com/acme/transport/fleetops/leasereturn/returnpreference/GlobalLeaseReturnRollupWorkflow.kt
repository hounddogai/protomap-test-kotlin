package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.fleetops.clock.Clock

/** Moves global lease return rollups through their life cycle and keeps a journal of what it did. */
data class GlobalLeaseReturnRollupWorkflow(
    var store: GlobalLeaseReturnRollupStore,
    var validator: GlobalLeaseReturnRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global lease return rollup $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalLeaseReturnRollupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global lease return rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalLeaseReturnRollupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newGlobalLeaseReturnRollupWorkflow(store: GlobalLeaseReturnRollupStore, clock: Clock): GlobalLeaseReturnRollupWorkflow {
    return GlobalLeaseReturnRollupWorkflow(
        store = store,
        validator = defaultGlobalLeaseReturnRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
