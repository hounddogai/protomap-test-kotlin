package com.acme.transport.customs.broker.entry

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary broker entries by code. */
interface PrimaryBrokerEntryStore {
    fun find(code: String): PrimaryBrokerEntry?
    fun save(item: PrimaryBrokerEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryBrokerEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryBrokerEntryStore(
    var items: MutableMap<String, PrimaryBrokerEntry>,
    var order: MutableList<String>,
) : PrimaryBrokerEntryStore {
    override fun find(code: String): PrimaryBrokerEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryBrokerEntry) {
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

    override fun list(): MutableList<PrimaryBrokerEntry> {
        val out = mutableListOf<PrimaryBrokerEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryBrokerEntryStore(): PrimaryBrokerEntryStore {
    return MemoryPrimaryBrokerEntryStore(mutableMapOf<String, PrimaryBrokerEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryBrokerEntryStore(
    var inner: PrimaryBrokerEntryStore,
    var cache: TtlCache<PrimaryBrokerEntry>,
    var hits: Int,
    var misses: Int,
) : PrimaryBrokerEntryStore {
    override fun find(code: String): PrimaryBrokerEntry? {
        val hit = (cache.get(code) as PrimaryBrokerEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryBrokerEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryBrokerEntry> = inner.list()
}

fun newCachedPrimaryBrokerEntryStore(inner: PrimaryBrokerEntryStore, ttl: Int, clock: Clock): PrimaryBrokerEntryStore {
    return CachedPrimaryBrokerEntryStore(
        inner = inner,
        cache = newTtlCache<PrimaryBrokerEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary broker entries. Empty fields match all. */
data class PrimaryBrokerEntryQuery(
    var country: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryBrokerEntry): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<PrimaryBrokerEntry>): MutableList<PrimaryBrokerEntry> {
        val matched = filter(items) { candidate: PrimaryBrokerEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryBrokerEntries(store: PrimaryBrokerEntryStore, query: PrimaryBrokerEntryQuery): MutableList<PrimaryBrokerEntry> {
    return query.apply(store.list())
}
