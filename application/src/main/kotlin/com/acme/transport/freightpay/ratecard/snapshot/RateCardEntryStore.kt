package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card entries by code. */
interface RateCardEntryStore {
    fun find(code: String): RateCardEntry?
    fun save(item: RateCardEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardEntryStore(
    var items: MutableMap<String, RateCardEntry>,
    var order: MutableList<String>,
) : RateCardEntryStore {
    override fun find(code: String): RateCardEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardEntry) {
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

    override fun list(): MutableList<RateCardEntry> {
        val out = mutableListOf<RateCardEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardEntryStore(): RateCardEntryStore {
    return MemoryRateCardEntryStore(mutableMapOf<String, RateCardEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardEntryStore(
    var inner: RateCardEntryStore,
    var cache: TtlCache<RateCardEntry>,
    var hits: Int,
    var misses: Int,
) : RateCardEntryStore {
    override fun find(code: String): RateCardEntry? {
        val hit = (cache.get(code) as RateCardEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardEntry> = inner.list()
}

fun newCachedRateCardEntryStore(inner: RateCardEntryStore, ttl: Int, clock: Clock): RateCardEntryStore {
    return CachedRateCardEntryStore(
        inner = inner,
        cache = newTtlCache<RateCardEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card entries. Empty fields match all. */
data class RateCardEntryQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardEntry): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RateCardEntry>): MutableList<RateCardEntry> {
        val matched = filter(items) { candidate: RateCardEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardEntries(store: RateCardEntryStore, query: RateCardEntryQuery): MutableList<RateCardEntry> {
    return query.apply(store.list())
}
