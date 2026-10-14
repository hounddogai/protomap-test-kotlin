package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident ledgers by code. */
interface IncidentLedgerStore {
    fun find(code: String): IncidentLedger?
    fun save(item: IncidentLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentLedgerStore(
    var items: MutableMap<String, IncidentLedger>,
    var order: MutableList<String>,
) : IncidentLedgerStore {
    override fun find(code: String): IncidentLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentLedger) {
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

    override fun list(): MutableList<IncidentLedger> {
        val out = mutableListOf<IncidentLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentLedgerStore(): IncidentLedgerStore {
    return MemoryIncidentLedgerStore(mutableMapOf<String, IncidentLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentLedgerStore(
    var inner: IncidentLedgerStore,
    var cache: TtlCache<IncidentLedger>,
    var hits: Int,
    var misses: Int,
) : IncidentLedgerStore {
    override fun find(code: String): IncidentLedger? {
        val hit = (cache.get(code) as IncidentLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentLedger> = inner.list()
}

fun newCachedIncidentLedgerStore(inner: IncidentLedgerStore, ttl: Int, clock: Clock): IncidentLedgerStore {
    return CachedIncidentLedgerStore(
        inner = inner,
        cache = newTtlCache<IncidentLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident ledgers. Empty fields match all. */
data class IncidentLedgerQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentLedger>): MutableList<IncidentLedger> {
        val matched = filter(items) { candidate: IncidentLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentLedgers(store: IncidentLedgerStore, query: IncidentLedgerQuery): MutableList<IncidentLedger> {
    return query.apply(store.list())
}
