package com.acme.transport.crewing.roster.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered roster exceptions by code. */
interface TieredRosterExceptionStore {
    fun find(code: String): TieredRosterException?
    fun save(item: TieredRosterException)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRosterException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRosterExceptionStore(
    var items: MutableMap<String, TieredRosterException>,
    var order: MutableList<String>,
) : TieredRosterExceptionStore {
    override fun find(code: String): TieredRosterException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRosterException) {
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

    override fun list(): MutableList<TieredRosterException> {
        val out = mutableListOf<TieredRosterException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRosterExceptionStore(): TieredRosterExceptionStore {
    return MemoryTieredRosterExceptionStore(
        mutableMapOf<String, TieredRosterException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRosterExceptionStore(
    var inner: TieredRosterExceptionStore,
    var cache: TtlCache<TieredRosterException>,
    var hits: Int,
    var misses: Int,
) : TieredRosterExceptionStore {
    override fun find(code: String): TieredRosterException? {
        val hit = (cache.get(code) as TieredRosterException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRosterException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRosterException> = inner.list()
}

fun newCachedTieredRosterExceptionStore(inner: TieredRosterExceptionStore, ttl: Int, clock: Clock): TieredRosterExceptionStore {
    return CachedTieredRosterExceptionStore(
        inner = inner,
        cache = newTtlCache<TieredRosterException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered roster exceptions. Empty fields match all. */
data class TieredRosterExceptionQuery(
    var segment: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRosterException): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<TieredRosterException>): MutableList<TieredRosterException> {
        val matched = filter(items) { candidate: TieredRosterException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRosterExceptions(store: TieredRosterExceptionStore, query: TieredRosterExceptionQuery): MutableList<TieredRosterException> {
    return query.apply(store.list())
}
