package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary accessorial entries by code. */
interface PrimaryAccessorialEntryStore {
    fun find(code: String): PrimaryAccessorialEntry?
    fun save(item: PrimaryAccessorialEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAccessorialEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAccessorialEntryStore(
    var items: MutableMap<String, PrimaryAccessorialEntry>,
    var order: MutableList<String>,
) : PrimaryAccessorialEntryStore {
    override fun find(code: String): PrimaryAccessorialEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAccessorialEntry) {
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

    override fun list(): MutableList<PrimaryAccessorialEntry> {
        val out = mutableListOf<PrimaryAccessorialEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAccessorialEntryStore(): PrimaryAccessorialEntryStore {
    return MemoryPrimaryAccessorialEntryStore(
        mutableMapOf<String, PrimaryAccessorialEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAccessorialEntryStore(
    var inner: PrimaryAccessorialEntryStore,
    var cache: TtlCache<PrimaryAccessorialEntry>,
    var hits: Int,
    var misses: Int,
) : PrimaryAccessorialEntryStore {
    override fun find(code: String): PrimaryAccessorialEntry? {
        val hit = (cache.get(code) as PrimaryAccessorialEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAccessorialEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAccessorialEntry> = inner.list()
}

fun newCachedPrimaryAccessorialEntryStore(inner: PrimaryAccessorialEntryStore, ttl: Int, clock: Clock): PrimaryAccessorialEntryStore {
    return CachedPrimaryAccessorialEntryStore(
        inner = inner,
        cache = newTtlCache<PrimaryAccessorialEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary accessorial entries. Empty fields match all. */
data class PrimaryAccessorialEntryQuery(
    var region: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAccessorialEntry): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryAccessorialEntry>): MutableList<PrimaryAccessorialEntry> {
        val matched = filter(items) { candidate: PrimaryAccessorialEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAccessorialEntries(store: PrimaryAccessorialEntryStore, query: PrimaryAccessorialEntryQuery): MutableList<PrimaryAccessorialEntry> {
    return query.apply(store.list())
}
