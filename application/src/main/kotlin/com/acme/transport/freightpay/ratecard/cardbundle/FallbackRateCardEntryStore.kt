package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback rate card entries by code. */
interface FallbackRateCardEntryStore {
    fun find(code: String): FallbackRateCardEntry?
    fun save(item: FallbackRateCardEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRateCardEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRateCardEntryStore(
    var items: MutableMap<String, FallbackRateCardEntry>,
    var order: MutableList<String>,
) : FallbackRateCardEntryStore {
    override fun find(code: String): FallbackRateCardEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRateCardEntry) {
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

    override fun list(): MutableList<FallbackRateCardEntry> {
        val out = mutableListOf<FallbackRateCardEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRateCardEntryStore(): FallbackRateCardEntryStore {
    return MemoryFallbackRateCardEntryStore(
        mutableMapOf<String, FallbackRateCardEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRateCardEntryStore(
    var inner: FallbackRateCardEntryStore,
    var cache: TtlCache<FallbackRateCardEntry>,
    var hits: Int,
    var misses: Int,
) : FallbackRateCardEntryStore {
    override fun find(code: String): FallbackRateCardEntry? {
        val hit = (cache.get(code) as FallbackRateCardEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRateCardEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRateCardEntry> = inner.list()
}

fun newCachedFallbackRateCardEntryStore(inner: FallbackRateCardEntryStore, ttl: Int, clock: Clock): FallbackRateCardEntryStore {
    return CachedFallbackRateCardEntryStore(
        inner = inner,
        cache = newTtlCache<FallbackRateCardEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback rate card entries. Empty fields match all. */
data class FallbackRateCardEntryQuery(
    var locale: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRateCardEntry): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRateCardEntry>): MutableList<FallbackRateCardEntry> {
        val matched = filter(items) { candidate: FallbackRateCardEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRateCardEntries(store: FallbackRateCardEntryStore, query: FallbackRateCardEntryQuery): MutableList<FallbackRateCardEntry> {
    return query.apply(store.list())
}
