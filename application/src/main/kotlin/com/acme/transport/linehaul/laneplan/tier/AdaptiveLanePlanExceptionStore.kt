package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive lane plan exceptions by code. */
interface AdaptiveLanePlanExceptionStore {
    fun find(code: String): AdaptiveLanePlanException?
    fun save(item: AdaptiveLanePlanException)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLanePlanException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLanePlanExceptionStore(
    var items: MutableMap<String, AdaptiveLanePlanException>,
    var order: MutableList<String>,
) : AdaptiveLanePlanExceptionStore {
    override fun find(code: String): AdaptiveLanePlanException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLanePlanException) {
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

    override fun list(): MutableList<AdaptiveLanePlanException> {
        val out = mutableListOf<AdaptiveLanePlanException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLanePlanExceptionStore(): AdaptiveLanePlanExceptionStore {
    return MemoryAdaptiveLanePlanExceptionStore(
        mutableMapOf<String, AdaptiveLanePlanException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLanePlanExceptionStore(
    var inner: AdaptiveLanePlanExceptionStore,
    var cache: TtlCache<AdaptiveLanePlanException>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLanePlanExceptionStore {
    override fun find(code: String): AdaptiveLanePlanException? {
        val hit = (cache.get(code) as AdaptiveLanePlanException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLanePlanException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLanePlanException> = inner.list()
}

fun newCachedAdaptiveLanePlanExceptionStore(inner: AdaptiveLanePlanExceptionStore, ttl: Int, clock: Clock): AdaptiveLanePlanExceptionStore {
    return CachedAdaptiveLanePlanExceptionStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLanePlanException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive lane plan exceptions. Empty fields match all. */
data class AdaptiveLanePlanExceptionQuery(
    var unit: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLanePlanException): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<AdaptiveLanePlanException>): MutableList<AdaptiveLanePlanException> {
        val matched = filter(items) { candidate: AdaptiveLanePlanException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLanePlanExceptions(store: AdaptiveLanePlanExceptionStore, query: AdaptiveLanePlanExceptionQuery): MutableList<AdaptiveLanePlanException> {
    return query.apply(store.list())
}
