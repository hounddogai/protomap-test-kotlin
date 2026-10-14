package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard exceptions by code. */
interface TrailerYardExceptionStore {
    fun find(code: String): TrailerYardException?
    fun save(item: TrailerYardException)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardExceptionStore(
    var items: MutableMap<String, TrailerYardException>,
    var order: MutableList<String>,
) : TrailerYardExceptionStore {
    override fun find(code: String): TrailerYardException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardException) {
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

    override fun list(): MutableList<TrailerYardException> {
        val out = mutableListOf<TrailerYardException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardExceptionStore(): TrailerYardExceptionStore {
    return MemoryTrailerYardExceptionStore(
        mutableMapOf<String, TrailerYardException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardExceptionStore(
    var inner: TrailerYardExceptionStore,
    var cache: TtlCache<TrailerYardException>,
    var hits: Int,
    var misses: Int,
) : TrailerYardExceptionStore {
    override fun find(code: String): TrailerYardException? {
        val hit = (cache.get(code) as TrailerYardException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardException> = inner.list()
}

fun newCachedTrailerYardExceptionStore(inner: TrailerYardExceptionStore, ttl: Int, clock: Clock): TrailerYardExceptionStore {
    return CachedTrailerYardExceptionStore(
        inner = inner,
        cache = newTtlCache<TrailerYardException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard exceptions. Empty fields match all. */
data class TrailerYardExceptionQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardException>): MutableList<TrailerYardException> {
        val matched = filter(items) { candidate: TrailerYardException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardExceptions(store: TrailerYardExceptionStore, query: TrailerYardExceptionQuery): MutableList<TrailerYardException> {
    return query.apply(store.list())
}
