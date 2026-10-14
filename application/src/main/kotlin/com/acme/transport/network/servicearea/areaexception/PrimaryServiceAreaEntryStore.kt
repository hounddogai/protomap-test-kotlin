package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary service area entries by code. */
interface PrimaryServiceAreaEntryStore {
    fun find(code: String): PrimaryServiceAreaEntry?
    fun save(item: PrimaryServiceAreaEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryServiceAreaEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryServiceAreaEntryStore(
    var items: MutableMap<String, PrimaryServiceAreaEntry>,
    var order: MutableList<String>,
) : PrimaryServiceAreaEntryStore {
    override fun find(code: String): PrimaryServiceAreaEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryServiceAreaEntry) {
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

    override fun list(): MutableList<PrimaryServiceAreaEntry> {
        val out = mutableListOf<PrimaryServiceAreaEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryServiceAreaEntryStore(): PrimaryServiceAreaEntryStore {
    return MemoryPrimaryServiceAreaEntryStore(
        mutableMapOf<String, PrimaryServiceAreaEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryServiceAreaEntryStore(
    var inner: PrimaryServiceAreaEntryStore,
    var cache: TtlCache<PrimaryServiceAreaEntry>,
    var hits: Int,
    var misses: Int,
) : PrimaryServiceAreaEntryStore {
    override fun find(code: String): PrimaryServiceAreaEntry? {
        val hit = (cache.get(code) as PrimaryServiceAreaEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryServiceAreaEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryServiceAreaEntry> = inner.list()
}

fun newCachedPrimaryServiceAreaEntryStore(inner: PrimaryServiceAreaEntryStore, ttl: Int, clock: Clock): PrimaryServiceAreaEntryStore {
    return CachedPrimaryServiceAreaEntryStore(
        inner = inner,
        cache = newTtlCache<PrimaryServiceAreaEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary service area entries. Empty fields match all. */
data class PrimaryServiceAreaEntryQuery(
    var country: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryServiceAreaEntry): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryServiceAreaEntry>): MutableList<PrimaryServiceAreaEntry> {
        val matched = filter(items) { candidate: PrimaryServiceAreaEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryServiceAreaEntries(store: PrimaryServiceAreaEntryStore, query: PrimaryServiceAreaEntryQuery): MutableList<PrimaryServiceAreaEntry> {
    return query.apply(store.list())
}
