package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer exceptions by code. */
interface TrailerExceptionStore {
    fun find(code: String): TrailerException?
    fun save(item: TrailerException)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerExceptionStore(
    var items: MutableMap<String, TrailerException>,
    var order: MutableList<String>,
) : TrailerExceptionStore {
    override fun find(code: String): TrailerException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerException) {
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

    override fun list(): MutableList<TrailerException> {
        val out = mutableListOf<TrailerException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerExceptionStore(): TrailerExceptionStore {
    return MemoryTrailerExceptionStore(mutableMapOf<String, TrailerException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerExceptionStore(
    var inner: TrailerExceptionStore,
    var cache: TtlCache<TrailerException>,
    var hits: Int,
    var misses: Int,
) : TrailerExceptionStore {
    override fun find(code: String): TrailerException? {
        val hit = (cache.get(code) as TrailerException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerException> = inner.list()
}

fun newCachedTrailerExceptionStore(inner: TrailerExceptionStore, ttl: Int, clock: Clock): TrailerExceptionStore {
    return CachedTrailerExceptionStore(
        inner = inner,
        cache = newTtlCache<TrailerException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer exceptions. Empty fields match all. */
data class TrailerExceptionQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerException): Boolean {
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

    fun apply(items: MutableList<TrailerException>): MutableList<TrailerException> {
        val matched = filter(items) { candidate: TrailerException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerExceptions(store: TrailerExceptionStore, query: TrailerExceptionQuery): MutableList<TrailerException> {
    return query.apply(store.list())
}
