package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive capacity entries by code. */
interface AdaptiveCapacityEntryStore {
    fun find(code: String): AdaptiveCapacityEntry?
    fun save(item: AdaptiveCapacityEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCapacityEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCapacityEntryStore(
    var items: MutableMap<String, AdaptiveCapacityEntry>,
    var order: MutableList<String>,
) : AdaptiveCapacityEntryStore {
    override fun find(code: String): AdaptiveCapacityEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCapacityEntry) {
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

    override fun list(): MutableList<AdaptiveCapacityEntry> {
        val out = mutableListOf<AdaptiveCapacityEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCapacityEntryStore(): AdaptiveCapacityEntryStore {
    return MemoryAdaptiveCapacityEntryStore(
        mutableMapOf<String, AdaptiveCapacityEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCapacityEntryStore(
    var inner: AdaptiveCapacityEntryStore,
    var cache: TtlCache<AdaptiveCapacityEntry>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCapacityEntryStore {
    override fun find(code: String): AdaptiveCapacityEntry? {
        val hit = (cache.get(code) as AdaptiveCapacityEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCapacityEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCapacityEntry> = inner.list()
}

fun newCachedAdaptiveCapacityEntryStore(inner: AdaptiveCapacityEntryStore, ttl: Int, clock: Clock): AdaptiveCapacityEntryStore {
    return CachedAdaptiveCapacityEntryStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCapacityEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive capacity entries. Empty fields match all. */
data class AdaptiveCapacityEntryQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCapacityEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCapacityEntry>): MutableList<AdaptiveCapacityEntry> {
        val matched = filter(items) { candidate: AdaptiveCapacityEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCapacityEntries(store: AdaptiveCapacityEntryStore, query: AdaptiveCapacityEntryQuery): MutableList<AdaptiveCapacityEntry> {
    return query.apply(store.list())
}
