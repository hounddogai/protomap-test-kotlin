package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss ledgers by code. */
interface NearMissLedgerStore {
    fun find(code: String): NearMissLedger?
    fun save(item: NearMissLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissLedgerStore(
    var items: MutableMap<String, NearMissLedger>,
    var order: MutableList<String>,
) : NearMissLedgerStore {
    override fun find(code: String): NearMissLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissLedger) {
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

    override fun list(): MutableList<NearMissLedger> {
        val out = mutableListOf<NearMissLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissLedgerStore(): NearMissLedgerStore {
    return MemoryNearMissLedgerStore(mutableMapOf<String, NearMissLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissLedgerStore(
    var inner: NearMissLedgerStore,
    var cache: TtlCache<NearMissLedger>,
    var hits: Int,
    var misses: Int,
) : NearMissLedgerStore {
    override fun find(code: String): NearMissLedger? {
        val hit = (cache.get(code) as NearMissLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissLedger> = inner.list()
}

fun newCachedNearMissLedgerStore(inner: NearMissLedgerStore, ttl: Int, clock: Clock): NearMissLedgerStore {
    return CachedNearMissLedgerStore(
        inner = inner,
        cache = newTtlCache<NearMissLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss ledgers. Empty fields match all. */
data class NearMissLedgerQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissLedger>): MutableList<NearMissLedger> {
        val matched = filter(items) { candidate: NearMissLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissLedgers(store: NearMissLedgerStore, query: NearMissLedgerQuery): MutableList<NearMissLedger> {
    return query.apply(store.list())
}
