package com.acme.transport.network.cutoff.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual cutoff ledgers by code. */
interface ManualCutoffLedgerStore {
    fun find(code: String): ManualCutoffLedger?
    fun save(item: ManualCutoffLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCutoffLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCutoffLedgerStore(
    var items: MutableMap<String, ManualCutoffLedger>,
    var order: MutableList<String>,
) : ManualCutoffLedgerStore {
    override fun find(code: String): ManualCutoffLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCutoffLedger) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<ManualCutoffLedger> {
        val out = mutableListOf<ManualCutoffLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCutoffLedgerStore(): ManualCutoffLedgerStore {
    return MemoryManualCutoffLedgerStore(mutableMapOf<String, ManualCutoffLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCutoffLedgerStore(
    var inner: ManualCutoffLedgerStore,
    var cache: TtlCache<ManualCutoffLedger>,
    var hits: Int,
    var misses: Int,
) : ManualCutoffLedgerStore {
    override fun find(code: String): ManualCutoffLedger? {
        val hit = (cache.get(code) as ManualCutoffLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCutoffLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCutoffLedger> = inner.list()
}

fun newCachedManualCutoffLedgerStore(inner: ManualCutoffLedgerStore, ttl: Int, clock: Clock): ManualCutoffLedgerStore {
    return CachedManualCutoffLedgerStore(
        inner = inner,
        cache = newTtlCache<ManualCutoffLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual cutoff ledgers. Empty fields match all. */
data class ManualCutoffLedgerQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCutoffLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCutoffLedger>): MutableList<ManualCutoffLedger> {
        val matched = filter(items) { candidate: ManualCutoffLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCutoffLedgers(store: ManualCutoffLedgerStore, query: ManualCutoffLedgerQuery): MutableList<ManualCutoffLedger> {
    return query.apply(store.list())
}
