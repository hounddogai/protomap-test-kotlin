package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive lane plan targets by code. */
interface AdaptiveLanePlanTargetStore {
    fun find(code: String): AdaptiveLanePlanTarget?
    fun save(item: AdaptiveLanePlanTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLanePlanTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLanePlanTargetStore(
    var items: MutableMap<String, AdaptiveLanePlanTarget>,
    var order: MutableList<String>,
) : AdaptiveLanePlanTargetStore {
    override fun find(code: String): AdaptiveLanePlanTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLanePlanTarget) {
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

    override fun list(): MutableList<AdaptiveLanePlanTarget> {
        val out = mutableListOf<AdaptiveLanePlanTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLanePlanTargetStore(): AdaptiveLanePlanTargetStore {
    return MemoryAdaptiveLanePlanTargetStore(
        mutableMapOf<String, AdaptiveLanePlanTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLanePlanTargetStore(
    var inner: AdaptiveLanePlanTargetStore,
    var cache: TtlCache<AdaptiveLanePlanTarget>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLanePlanTargetStore {
    override fun find(code: String): AdaptiveLanePlanTarget? {
        val hit = (cache.get(code) as AdaptiveLanePlanTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLanePlanTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLanePlanTarget> = inner.list()
}

fun newCachedAdaptiveLanePlanTargetStore(inner: AdaptiveLanePlanTargetStore, ttl: Int, clock: Clock): AdaptiveLanePlanTargetStore {
    return CachedAdaptiveLanePlanTargetStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLanePlanTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive lane plan targets. Empty fields match all. */
data class AdaptiveLanePlanTargetQuery(
    var category: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLanePlanTarget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveLanePlanTarget>): MutableList<AdaptiveLanePlanTarget> {
        val matched = filter(items) { candidate: AdaptiveLanePlanTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLanePlanTargets(store: AdaptiveLanePlanTargetStore, query: AdaptiveLanePlanTargetQuery): MutableList<AdaptiveLanePlanTarget> {
    return query.apply(store.list())
}
