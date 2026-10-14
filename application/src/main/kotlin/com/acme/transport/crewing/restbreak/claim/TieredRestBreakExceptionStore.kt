package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered rest break exceptions by code. */
interface TieredRestBreakExceptionStore {
    fun find(code: String): TieredRestBreakException?
    fun save(item: TieredRestBreakException)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRestBreakException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRestBreakExceptionStore(
    var items: MutableMap<String, TieredRestBreakException>,
    var order: MutableList<String>,
) : TieredRestBreakExceptionStore {
    override fun find(code: String): TieredRestBreakException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRestBreakException) {
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

    override fun list(): MutableList<TieredRestBreakException> {
        val out = mutableListOf<TieredRestBreakException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRestBreakExceptionStore(): TieredRestBreakExceptionStore {
    return MemoryTieredRestBreakExceptionStore(
        mutableMapOf<String, TieredRestBreakException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRestBreakExceptionStore(
    var inner: TieredRestBreakExceptionStore,
    var cache: TtlCache<TieredRestBreakException>,
    var hits: Int,
    var misses: Int,
) : TieredRestBreakExceptionStore {
    override fun find(code: String): TieredRestBreakException? {
        val hit = (cache.get(code) as TieredRestBreakException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRestBreakException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRestBreakException> = inner.list()
}

fun newCachedTieredRestBreakExceptionStore(inner: TieredRestBreakExceptionStore, ttl: Int, clock: Clock): TieredRestBreakExceptionStore {
    return CachedTieredRestBreakExceptionStore(
        inner = inner,
        cache = newTtlCache<TieredRestBreakException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered rest break exceptions. Empty fields match all. */
data class TieredRestBreakExceptionQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRestBreakException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRestBreakException>): MutableList<TieredRestBreakException> {
        val matched = filter(items) { candidate: TieredRestBreakException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRestBreakExceptions(store: TieredRestBreakExceptionStore, query: TieredRestBreakExceptionQuery): MutableList<TieredRestBreakException> {
    return query.apply(store.list())
}
