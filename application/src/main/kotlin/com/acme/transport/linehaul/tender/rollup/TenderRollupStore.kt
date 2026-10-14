package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender rollups by code. */
interface TenderRollupStore {
    fun find(code: String): TenderRollup?
    fun save(item: TenderRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderRollupStore(
    var items: MutableMap<String, TenderRollup>,
    var order: MutableList<String>,
) : TenderRollupStore {
    override fun find(code: String): TenderRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderRollup) {
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

    override fun list(): MutableList<TenderRollup> {
        val out = mutableListOf<TenderRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderRollupStore(): TenderRollupStore {
    return MemoryTenderRollupStore(mutableMapOf<String, TenderRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderRollupStore(
    var inner: TenderRollupStore,
    var cache: TtlCache<TenderRollup>,
    var hits: Int,
    var misses: Int,
) : TenderRollupStore {
    override fun find(code: String): TenderRollup? {
        val hit = (cache.get(code) as TenderRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderRollup> = inner.list()
}

fun newCachedTenderRollupStore(inner: TenderRollupStore, ttl: Int, clock: Clock): TenderRollupStore {
    return CachedTenderRollupStore(
        inner = inner,
        cache = newTtlCache<TenderRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender rollups. Empty fields match all. */
data class TenderRollupQuery(
    var locale: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderRollup): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderRollup>): MutableList<TenderRollup> {
        val matched = filter(items) { candidate: TenderRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderRollups(store: TenderRollupStore, query: TenderRollupQuery): MutableList<TenderRollup> {
    return query.apply(store.list())
}
