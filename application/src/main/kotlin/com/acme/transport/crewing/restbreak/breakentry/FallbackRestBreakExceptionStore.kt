package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback rest break exceptions by code. */
interface FallbackRestBreakExceptionStore {
    fun find(code: String): FallbackRestBreakException?
    fun save(item: FallbackRestBreakException)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRestBreakException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRestBreakExceptionStore(
    var items: MutableMap<String, FallbackRestBreakException>,
    var order: MutableList<String>,
) : FallbackRestBreakExceptionStore {
    override fun find(code: String): FallbackRestBreakException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRestBreakException) {
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

    override fun list(): MutableList<FallbackRestBreakException> {
        val out = mutableListOf<FallbackRestBreakException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRestBreakExceptionStore(): FallbackRestBreakExceptionStore {
    return MemoryFallbackRestBreakExceptionStore(
        mutableMapOf<String, FallbackRestBreakException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRestBreakExceptionStore(
    var inner: FallbackRestBreakExceptionStore,
    var cache: TtlCache<FallbackRestBreakException>,
    var hits: Int,
    var misses: Int,
) : FallbackRestBreakExceptionStore {
    override fun find(code: String): FallbackRestBreakException? {
        val hit = (cache.get(code) as FallbackRestBreakException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRestBreakException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRestBreakException> = inner.list()
}

fun newCachedFallbackRestBreakExceptionStore(inner: FallbackRestBreakExceptionStore, ttl: Int, clock: Clock): FallbackRestBreakExceptionStore {
    return CachedFallbackRestBreakExceptionStore(
        inner = inner,
        cache = newTtlCache<FallbackRestBreakException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback rest break exceptions. Empty fields match all. */
data class FallbackRestBreakExceptionQuery(
    var locale: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRestBreakException): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRestBreakException>): MutableList<FallbackRestBreakException> {
        val matched = filter(items) { candidate: FallbackRestBreakException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRestBreakExceptions(store: FallbackRestBreakExceptionStore, query: FallbackRestBreakExceptionQuery): MutableList<FallbackRestBreakException> {
    return query.apply(store.list())
}
