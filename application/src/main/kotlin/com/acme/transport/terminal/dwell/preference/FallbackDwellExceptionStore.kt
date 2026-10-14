package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback dwell exceptions by code. */
interface FallbackDwellExceptionStore {
    fun find(code: String): FallbackDwellException?
    fun save(item: FallbackDwellException)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDwellException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDwellExceptionStore(
    var items: MutableMap<String, FallbackDwellException>,
    var order: MutableList<String>,
) : FallbackDwellExceptionStore {
    override fun find(code: String): FallbackDwellException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDwellException) {
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

    override fun list(): MutableList<FallbackDwellException> {
        val out = mutableListOf<FallbackDwellException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDwellExceptionStore(): FallbackDwellExceptionStore {
    return MemoryFallbackDwellExceptionStore(
        mutableMapOf<String, FallbackDwellException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDwellExceptionStore(
    var inner: FallbackDwellExceptionStore,
    var cache: TtlCache<FallbackDwellException>,
    var hits: Int,
    var misses: Int,
) : FallbackDwellExceptionStore {
    override fun find(code: String): FallbackDwellException? {
        val hit = (cache.get(code) as FallbackDwellException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDwellException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDwellException> = inner.list()
}

fun newCachedFallbackDwellExceptionStore(inner: FallbackDwellExceptionStore, ttl: Int, clock: Clock): FallbackDwellExceptionStore {
    return CachedFallbackDwellExceptionStore(
        inner = inner,
        cache = newTtlCache<FallbackDwellException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dwell exceptions. Empty fields match all. */
data class FallbackDwellExceptionQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDwellException): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<FallbackDwellException>): MutableList<FallbackDwellException> {
        val matched = filter(items) { candidate: FallbackDwellException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDwellExceptions(store: FallbackDwellExceptionStore, query: FallbackDwellExceptionQuery): MutableList<FallbackDwellException> {
    return query.apply(store.list())
}
