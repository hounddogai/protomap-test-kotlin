package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break exceptions by code. */
interface RestBreakExceptionStore {
    fun find(code: String): RestBreakException?
    fun save(item: RestBreakException)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakExceptionStore(
    var items: MutableMap<String, RestBreakException>,
    var order: MutableList<String>,
) : RestBreakExceptionStore {
    override fun find(code: String): RestBreakException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakException) {
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

    override fun list(): MutableList<RestBreakException> {
        val out = mutableListOf<RestBreakException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakExceptionStore(): RestBreakExceptionStore {
    return MemoryRestBreakExceptionStore(mutableMapOf<String, RestBreakException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakExceptionStore(
    var inner: RestBreakExceptionStore,
    var cache: TtlCache<RestBreakException>,
    var hits: Int,
    var misses: Int,
) : RestBreakExceptionStore {
    override fun find(code: String): RestBreakException? {
        val hit = (cache.get(code) as RestBreakException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakException> = inner.list()
}

fun newCachedRestBreakExceptionStore(inner: RestBreakExceptionStore, ttl: Int, clock: Clock): RestBreakExceptionStore {
    return CachedRestBreakExceptionStore(
        inner = inner,
        cache = newTtlCache<RestBreakException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break exceptions. Empty fields match all. */
data class RestBreakExceptionQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakException): Boolean {
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

    fun apply(items: MutableList<RestBreakException>): MutableList<RestBreakException> {
        val matched = filter(items) { candidate: RestBreakException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakExceptions(store: RestBreakExceptionStore, query: RestBreakExceptionQuery): MutableList<RestBreakException> {
    return query.apply(store.list())
}
