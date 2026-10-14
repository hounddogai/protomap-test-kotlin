package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback attempt exceptions by code. */
interface FallbackAttemptExceptionStore {
    fun find(code: String): FallbackAttemptException?
    fun save(item: FallbackAttemptException)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAttemptException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAttemptExceptionStore(
    var items: MutableMap<String, FallbackAttemptException>,
    var order: MutableList<String>,
) : FallbackAttemptExceptionStore {
    override fun find(code: String): FallbackAttemptException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAttemptException) {
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

    override fun list(): MutableList<FallbackAttemptException> {
        val out = mutableListOf<FallbackAttemptException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAttemptExceptionStore(): FallbackAttemptExceptionStore {
    return MemoryFallbackAttemptExceptionStore(
        mutableMapOf<String, FallbackAttemptException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAttemptExceptionStore(
    var inner: FallbackAttemptExceptionStore,
    var cache: TtlCache<FallbackAttemptException>,
    var hits: Int,
    var misses: Int,
) : FallbackAttemptExceptionStore {
    override fun find(code: String): FallbackAttemptException? {
        val hit = (cache.get(code) as FallbackAttemptException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAttemptException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAttemptException> = inner.list()
}

fun newCachedFallbackAttemptExceptionStore(inner: FallbackAttemptExceptionStore, ttl: Int, clock: Clock): FallbackAttemptExceptionStore {
    return CachedFallbackAttemptExceptionStore(
        inner = inner,
        cache = newTtlCache<FallbackAttemptException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback attempt exceptions. Empty fields match all. */
data class FallbackAttemptExceptionQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAttemptException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAttemptException>): MutableList<FallbackAttemptException> {
        val matched = filter(items) { candidate: FallbackAttemptException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAttemptExceptions(store: FallbackAttemptExceptionStore, query: FallbackAttemptExceptionQuery): MutableList<FallbackAttemptException> {
    return query.apply(store.list())
}
