package com.acme.transport.freightpay.fuelsurcharge.surchargeentry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default fuel surcharge entries by code. */
interface DefaultFuelSurchargeEntryStore {
    fun find(code: String): DefaultFuelSurchargeEntry?
    fun save(item: DefaultFuelSurchargeEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultFuelSurchargeEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultFuelSurchargeEntryStore(
    var items: MutableMap<String, DefaultFuelSurchargeEntry>,
    var order: MutableList<String>,
) : DefaultFuelSurchargeEntryStore {
    override fun find(code: String): DefaultFuelSurchargeEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultFuelSurchargeEntry) {
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

    override fun list(): MutableList<DefaultFuelSurchargeEntry> {
        val out = mutableListOf<DefaultFuelSurchargeEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultFuelSurchargeEntryStore(): DefaultFuelSurchargeEntryStore {
    return MemoryDefaultFuelSurchargeEntryStore(
        mutableMapOf<String, DefaultFuelSurchargeEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultFuelSurchargeEntryStore(
    var inner: DefaultFuelSurchargeEntryStore,
    var cache: TtlCache<DefaultFuelSurchargeEntry>,
    var hits: Int,
    var misses: Int,
) : DefaultFuelSurchargeEntryStore {
    override fun find(code: String): DefaultFuelSurchargeEntry? {
        val hit = (cache.get(code) as DefaultFuelSurchargeEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultFuelSurchargeEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultFuelSurchargeEntry> = inner.list()
}

fun newCachedDefaultFuelSurchargeEntryStore(inner: DefaultFuelSurchargeEntryStore, ttl: Int, clock: Clock): DefaultFuelSurchargeEntryStore {
    return CachedDefaultFuelSurchargeEntryStore(
        inner = inner,
        cache = newTtlCache<DefaultFuelSurchargeEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default fuel surcharge entries. Empty fields match all. */
data class DefaultFuelSurchargeEntryQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultFuelSurchargeEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultFuelSurchargeEntry>): MutableList<DefaultFuelSurchargeEntry> {
        val matched = filter(items) { candidate: DefaultFuelSurchargeEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultFuelSurchargeEntries(store: DefaultFuelSurchargeEntryStore, query: DefaultFuelSurchargeEntryQuery): MutableList<DefaultFuelSurchargeEntry> {
    return query.apply(store.list())
}
