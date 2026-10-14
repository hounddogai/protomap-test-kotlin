package com.acme.transport.customs.broker.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker entries by code. */
interface BrokerEntryStore {
    fun find(code: String): BrokerEntry?
    fun save(item: BrokerEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerEntryStore(
    var items: MutableMap<String, BrokerEntry>,
    var order: MutableList<String>,
) : BrokerEntryStore {
    override fun find(code: String): BrokerEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerEntry) {
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

    override fun list(): MutableList<BrokerEntry> {
        val out = mutableListOf<BrokerEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerEntryStore(): BrokerEntryStore {
    return MemoryBrokerEntryStore(mutableMapOf<String, BrokerEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerEntryStore(
    var inner: BrokerEntryStore,
    var cache: TtlCache<BrokerEntry>,
    var hits: Int,
    var misses: Int,
) : BrokerEntryStore {
    override fun find(code: String): BrokerEntry? {
        val hit = (cache.get(code) as BrokerEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerEntry> = inner.list()
}

fun newCachedBrokerEntryStore(inner: BrokerEntryStore, ttl: Int, clock: Clock): BrokerEntryStore {
    return CachedBrokerEntryStore(
        inner = inner,
        cache = newTtlCache<BrokerEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker entries. Empty fields match all. */
data class BrokerEntryQuery(
    var source: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerEntry): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerEntry>): MutableList<BrokerEntry> {
        val matched = filter(items) { candidate: BrokerEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerEntries(store: BrokerEntryStore, query: BrokerEntryQuery): MutableList<BrokerEntry> {
    return query.apply(store.list())
}
