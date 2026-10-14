package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling idling ledgers by code. */
interface RollingIdlingLedgerStore {
    fun find(code: String): RollingIdlingLedger?
    fun save(item: RollingIdlingLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingIdlingLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingIdlingLedgerStore(
    var items: MutableMap<String, RollingIdlingLedger>,
    var order: MutableList<String>,
) : RollingIdlingLedgerStore {
    override fun find(code: String): RollingIdlingLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingIdlingLedger) {
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

    override fun list(): MutableList<RollingIdlingLedger> {
        val out = mutableListOf<RollingIdlingLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingIdlingLedgerStore(): RollingIdlingLedgerStore {
    return MemoryRollingIdlingLedgerStore(mutableMapOf<String, RollingIdlingLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingIdlingLedgerStore(
    var inner: RollingIdlingLedgerStore,
    var cache: TtlCache<RollingIdlingLedger>,
    var hits: Int,
    var misses: Int,
) : RollingIdlingLedgerStore {
    override fun find(code: String): RollingIdlingLedger? {
        val hit = (cache.get(code) as RollingIdlingLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingIdlingLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingIdlingLedger> = inner.list()
}

fun newCachedRollingIdlingLedgerStore(inner: RollingIdlingLedgerStore, ttl: Int, clock: Clock): RollingIdlingLedgerStore {
    return CachedRollingIdlingLedgerStore(
        inner = inner,
        cache = newTtlCache<RollingIdlingLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling idling ledgers. Empty fields match all. */
data class RollingIdlingLedgerQuery(
    var source: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingIdlingLedger): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingIdlingLedger>): MutableList<RollingIdlingLedger> {
        val matched = filter(items) { candidate: RollingIdlingLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingIdlingLedgers(store: RollingIdlingLedgerStore, query: RollingIdlingLedgerQuery): MutableList<RollingIdlingLedger> {
    return query.apply(store.list())
}
