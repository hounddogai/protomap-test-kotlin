package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive near miss exceptions by code. */
interface AdaptiveNearMissExceptionStore {
    fun find(code: String): AdaptiveNearMissException?
    fun save(item: AdaptiveNearMissException)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveNearMissException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveNearMissExceptionStore(
    var items: MutableMap<String, AdaptiveNearMissException>,
    var order: MutableList<String>,
) : AdaptiveNearMissExceptionStore {
    override fun find(code: String): AdaptiveNearMissException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveNearMissException) {
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

    override fun list(): MutableList<AdaptiveNearMissException> {
        val out = mutableListOf<AdaptiveNearMissException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveNearMissExceptionStore(): AdaptiveNearMissExceptionStore {
    return MemoryAdaptiveNearMissExceptionStore(
        mutableMapOf<String, AdaptiveNearMissException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveNearMissExceptionStore(
    var inner: AdaptiveNearMissExceptionStore,
    var cache: TtlCache<AdaptiveNearMissException>,
    var hits: Int,
    var misses: Int,
) : AdaptiveNearMissExceptionStore {
    override fun find(code: String): AdaptiveNearMissException? {
        val hit = (cache.get(code) as AdaptiveNearMissException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveNearMissException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveNearMissException> = inner.list()
}

fun newCachedAdaptiveNearMissExceptionStore(inner: AdaptiveNearMissExceptionStore, ttl: Int, clock: Clock): AdaptiveNearMissExceptionStore {
    return CachedAdaptiveNearMissExceptionStore(
        inner = inner,
        cache = newTtlCache<AdaptiveNearMissException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive near miss exceptions. Empty fields match all. */
data class AdaptiveNearMissExceptionQuery(
    var category: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveNearMissException): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveNearMissException>): MutableList<AdaptiveNearMissException> {
        val matched = filter(items) { candidate: AdaptiveNearMissException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveNearMissExceptions(store: AdaptiveNearMissExceptionStore, query: AdaptiveNearMissExceptionQuery): MutableList<AdaptiveNearMissException> {
    return query.apply(store.list())
}
