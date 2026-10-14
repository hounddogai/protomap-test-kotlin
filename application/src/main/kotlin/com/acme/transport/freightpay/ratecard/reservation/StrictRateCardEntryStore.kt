package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict rate card entries by code. */
interface StrictRateCardEntryStore {
    fun find(code: String): StrictRateCardEntry?
    fun save(item: StrictRateCardEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRateCardEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRateCardEntryStore(
    var items: MutableMap<String, StrictRateCardEntry>,
    var order: MutableList<String>,
) : StrictRateCardEntryStore {
    override fun find(code: String): StrictRateCardEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRateCardEntry) {
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

    override fun list(): MutableList<StrictRateCardEntry> {
        val out = mutableListOf<StrictRateCardEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRateCardEntryStore(): StrictRateCardEntryStore {
    return MemoryStrictRateCardEntryStore(mutableMapOf<String, StrictRateCardEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRateCardEntryStore(
    var inner: StrictRateCardEntryStore,
    var cache: TtlCache<StrictRateCardEntry>,
    var hits: Int,
    var misses: Int,
) : StrictRateCardEntryStore {
    override fun find(code: String): StrictRateCardEntry? {
        val hit = (cache.get(code) as StrictRateCardEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRateCardEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRateCardEntry> = inner.list()
}

fun newCachedStrictRateCardEntryStore(inner: StrictRateCardEntryStore, ttl: Int, clock: Clock): StrictRateCardEntryStore {
    return CachedStrictRateCardEntryStore(
        inner = inner,
        cache = newTtlCache<StrictRateCardEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict rate card entries. Empty fields match all. */
data class StrictRateCardEntryQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRateCardEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRateCardEntry>): MutableList<StrictRateCardEntry> {
        val matched = filter(items) { candidate: StrictRateCardEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRateCardEntries(store: StrictRateCardEntryStore, query: StrictRateCardEntryQuery): MutableList<StrictRateCardEntry> {
    return query.apply(store.list())
}
