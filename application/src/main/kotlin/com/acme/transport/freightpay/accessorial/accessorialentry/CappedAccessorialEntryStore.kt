package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped accessorial entries by code. */
interface CappedAccessorialEntryStore {
    fun find(code: String): CappedAccessorialEntry?
    fun save(item: CappedAccessorialEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedAccessorialEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedAccessorialEntryStore(
    var items: MutableMap<String, CappedAccessorialEntry>,
    var order: MutableList<String>,
) : CappedAccessorialEntryStore {
    override fun find(code: String): CappedAccessorialEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedAccessorialEntry) {
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

    override fun list(): MutableList<CappedAccessorialEntry> {
        val out = mutableListOf<CappedAccessorialEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedAccessorialEntryStore(): CappedAccessorialEntryStore {
    return MemoryCappedAccessorialEntryStore(
        mutableMapOf<String, CappedAccessorialEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedAccessorialEntryStore(
    var inner: CappedAccessorialEntryStore,
    var cache: TtlCache<CappedAccessorialEntry>,
    var hits: Int,
    var misses: Int,
) : CappedAccessorialEntryStore {
    override fun find(code: String): CappedAccessorialEntry? {
        val hit = (cache.get(code) as CappedAccessorialEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedAccessorialEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedAccessorialEntry> = inner.list()
}

fun newCachedCappedAccessorialEntryStore(inner: CappedAccessorialEntryStore, ttl: Int, clock: Clock): CappedAccessorialEntryStore {
    return CachedCappedAccessorialEntryStore(
        inner = inner,
        cache = newTtlCache<CappedAccessorialEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped accessorial entries. Empty fields match all. */
data class CappedAccessorialEntryQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedAccessorialEntry): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedAccessorialEntry>): MutableList<CappedAccessorialEntry> {
        val matched = filter(items) { candidate: CappedAccessorialEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedAccessorialEntries(store: CappedAccessorialEntryStore, query: CappedAccessorialEntryQuery): MutableList<CappedAccessorialEntry> {
    return query.apply(store.list())
}
