package com.acme.transport.customs.hold.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback hold entries by code. */
interface FallbackHoldEntryStore {
    fun find(code: String): FallbackHoldEntry?
    fun save(item: FallbackHoldEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackHoldEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackHoldEntryStore(
    var items: MutableMap<String, FallbackHoldEntry>,
    var order: MutableList<String>,
) : FallbackHoldEntryStore {
    override fun find(code: String): FallbackHoldEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackHoldEntry) {
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

    override fun list(): MutableList<FallbackHoldEntry> {
        val out = mutableListOf<FallbackHoldEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackHoldEntryStore(): FallbackHoldEntryStore {
    return MemoryFallbackHoldEntryStore(mutableMapOf<String, FallbackHoldEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackHoldEntryStore(
    var inner: FallbackHoldEntryStore,
    var cache: TtlCache<FallbackHoldEntry>,
    var hits: Int,
    var misses: Int,
) : FallbackHoldEntryStore {
    override fun find(code: String): FallbackHoldEntry? {
        val hit = (cache.get(code) as FallbackHoldEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackHoldEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackHoldEntry> = inner.list()
}

fun newCachedFallbackHoldEntryStore(inner: FallbackHoldEntryStore, ttl: Int, clock: Clock): FallbackHoldEntryStore {
    return CachedFallbackHoldEntryStore(
        inner = inner,
        cache = newTtlCache<FallbackHoldEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback hold entries. Empty fields match all. */
data class FallbackHoldEntryQuery(
    var category: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackHoldEntry): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackHoldEntry>): MutableList<FallbackHoldEntry> {
        val matched = filter(items) { candidate: FallbackHoldEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackHoldEntries(store: FallbackHoldEntryStore, query: FallbackHoldEntryQuery): MutableList<FallbackHoldEntry> {
    return query.apply(store.list())
}
