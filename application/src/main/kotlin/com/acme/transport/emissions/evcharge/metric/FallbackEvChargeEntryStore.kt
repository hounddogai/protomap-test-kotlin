package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback ev charge entries by code. */
interface FallbackEvChargeEntryStore {
    fun find(code: String): FallbackEvChargeEntry?
    fun save(item: FallbackEvChargeEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEvChargeEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEvChargeEntryStore(
    var items: MutableMap<String, FallbackEvChargeEntry>,
    var order: MutableList<String>,
) : FallbackEvChargeEntryStore {
    override fun find(code: String): FallbackEvChargeEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEvChargeEntry) {
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

    override fun list(): MutableList<FallbackEvChargeEntry> {
        val out = mutableListOf<FallbackEvChargeEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEvChargeEntryStore(): FallbackEvChargeEntryStore {
    return MemoryFallbackEvChargeEntryStore(
        mutableMapOf<String, FallbackEvChargeEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEvChargeEntryStore(
    var inner: FallbackEvChargeEntryStore,
    var cache: TtlCache<FallbackEvChargeEntry>,
    var hits: Int,
    var misses: Int,
) : FallbackEvChargeEntryStore {
    override fun find(code: String): FallbackEvChargeEntry? {
        val hit = (cache.get(code) as FallbackEvChargeEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEvChargeEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEvChargeEntry> = inner.list()
}

fun newCachedFallbackEvChargeEntryStore(inner: FallbackEvChargeEntryStore, ttl: Int, clock: Clock): FallbackEvChargeEntryStore {
    return CachedFallbackEvChargeEntryStore(
        inner = inner,
        cache = newTtlCache<FallbackEvChargeEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback ev charge entries. Empty fields match all. */
data class FallbackEvChargeEntryQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEvChargeEntry): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEvChargeEntry>): MutableList<FallbackEvChargeEntry> {
        val matched = filter(items) { candidate: FallbackEvChargeEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEvChargeEntries(store: FallbackEvChargeEntryStore, query: FallbackEvChargeEntryQuery): MutableList<FallbackEvChargeEntry> {
    return query.apply(store.list())
}
