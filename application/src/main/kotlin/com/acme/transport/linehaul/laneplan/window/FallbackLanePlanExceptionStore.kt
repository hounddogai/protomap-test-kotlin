package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback lane plan exceptions by code. */
interface FallbackLanePlanExceptionStore {
    fun find(code: String): FallbackLanePlanException?
    fun save(item: FallbackLanePlanException)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackLanePlanException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackLanePlanExceptionStore(
    var items: MutableMap<String, FallbackLanePlanException>,
    var order: MutableList<String>,
) : FallbackLanePlanExceptionStore {
    override fun find(code: String): FallbackLanePlanException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackLanePlanException) {
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

    override fun list(): MutableList<FallbackLanePlanException> {
        val out = mutableListOf<FallbackLanePlanException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackLanePlanExceptionStore(): FallbackLanePlanExceptionStore {
    return MemoryFallbackLanePlanExceptionStore(
        mutableMapOf<String, FallbackLanePlanException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackLanePlanExceptionStore(
    var inner: FallbackLanePlanExceptionStore,
    var cache: TtlCache<FallbackLanePlanException>,
    var hits: Int,
    var misses: Int,
) : FallbackLanePlanExceptionStore {
    override fun find(code: String): FallbackLanePlanException? {
        val hit = (cache.get(code) as FallbackLanePlanException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackLanePlanException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackLanePlanException> = inner.list()
}

fun newCachedFallbackLanePlanExceptionStore(inner: FallbackLanePlanExceptionStore, ttl: Int, clock: Clock): FallbackLanePlanExceptionStore {
    return CachedFallbackLanePlanExceptionStore(
        inner = inner,
        cache = newTtlCache<FallbackLanePlanException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback lane plan exceptions. Empty fields match all. */
data class FallbackLanePlanExceptionQuery(
    var source: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackLanePlanException): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackLanePlanException>): MutableList<FallbackLanePlanException> {
        val matched = filter(items) { candidate: FallbackLanePlanException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackLanePlanExceptions(store: FallbackLanePlanExceptionStore, query: FallbackLanePlanExceptionQuery): MutableList<FallbackLanePlanException> {
    return query.apply(store.list())
}
