package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling rate card entries by code. */
interface RollingRateCardEntryStore {
    fun find(code: String): RollingRateCardEntry?
    fun save(item: RollingRateCardEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRateCardEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRateCardEntryStore(
    var items: MutableMap<String, RollingRateCardEntry>,
    var order: MutableList<String>,
) : RollingRateCardEntryStore {
    override fun find(code: String): RollingRateCardEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRateCardEntry) {
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

    override fun list(): MutableList<RollingRateCardEntry> {
        val out = mutableListOf<RollingRateCardEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRateCardEntryStore(): RollingRateCardEntryStore {
    return MemoryRollingRateCardEntryStore(
        mutableMapOf<String, RollingRateCardEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRateCardEntryStore(
    var inner: RollingRateCardEntryStore,
    var cache: TtlCache<RollingRateCardEntry>,
    var hits: Int,
    var misses: Int,
) : RollingRateCardEntryStore {
    override fun find(code: String): RollingRateCardEntry? {
        val hit = (cache.get(code) as RollingRateCardEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRateCardEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRateCardEntry> = inner.list()
}

fun newCachedRollingRateCardEntryStore(inner: RollingRateCardEntryStore, ttl: Int, clock: Clock): RollingRateCardEntryStore {
    return CachedRollingRateCardEntryStore(
        inner = inner,
        cache = newTtlCache<RollingRateCardEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling rate card entries. Empty fields match all. */
data class RollingRateCardEntryQuery(
    var category: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRateCardEntry): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRateCardEntry>): MutableList<RollingRateCardEntry> {
        val matched = filter(items) { candidate: RollingRateCardEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRateCardEntries(store: RollingRateCardEntryStore, query: RollingRateCardEntryQuery): MutableList<RollingRateCardEntry> {
    return query.apply(store.list())
}
