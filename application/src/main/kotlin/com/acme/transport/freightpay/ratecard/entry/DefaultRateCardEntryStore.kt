package com.acme.transport.freightpay.ratecard.entry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default rate card entries by code. */
interface DefaultRateCardEntryStore {
    fun find(code: String): DefaultRateCardEntry?
    fun save(item: DefaultRateCardEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRateCardEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRateCardEntryStore(
    var items: MutableMap<String, DefaultRateCardEntry>,
    var order: MutableList<String>,
) : DefaultRateCardEntryStore {
    override fun find(code: String): DefaultRateCardEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRateCardEntry) {
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

    override fun list(): MutableList<DefaultRateCardEntry> {
        val out = mutableListOf<DefaultRateCardEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRateCardEntryStore(): DefaultRateCardEntryStore {
    return MemoryDefaultRateCardEntryStore(
        mutableMapOf<String, DefaultRateCardEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRateCardEntryStore(
    var inner: DefaultRateCardEntryStore,
    var cache: TtlCache<DefaultRateCardEntry>,
    var hits: Int,
    var misses: Int,
) : DefaultRateCardEntryStore {
    override fun find(code: String): DefaultRateCardEntry? {
        val hit = (cache.get(code) as DefaultRateCardEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRateCardEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRateCardEntry> = inner.list()
}

fun newCachedDefaultRateCardEntryStore(inner: DefaultRateCardEntryStore, ttl: Int, clock: Clock): DefaultRateCardEntryStore {
    return CachedDefaultRateCardEntryStore(
        inner = inner,
        cache = newTtlCache<DefaultRateCardEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default rate card entries. Empty fields match all. */
data class DefaultRateCardEntryQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRateCardEntry): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRateCardEntry>): MutableList<DefaultRateCardEntry> {
        val matched = filter(items) { candidate: DefaultRateCardEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRateCardEntries(store: DefaultRateCardEntryStore, query: DefaultRateCardEntryQuery): MutableList<DefaultRateCardEntry> {
    return query.apply(store.list())
}
