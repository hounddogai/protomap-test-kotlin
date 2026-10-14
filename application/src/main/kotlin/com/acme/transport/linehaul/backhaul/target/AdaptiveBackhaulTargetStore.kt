package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive backhaul targets by code. */
interface AdaptiveBackhaulTargetStore {
    fun find(code: String): AdaptiveBackhaulTarget?
    fun save(item: AdaptiveBackhaulTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveBackhaulTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveBackhaulTargetStore(
    var items: MutableMap<String, AdaptiveBackhaulTarget>,
    var order: MutableList<String>,
) : AdaptiveBackhaulTargetStore {
    override fun find(code: String): AdaptiveBackhaulTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveBackhaulTarget) {
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

    override fun list(): MutableList<AdaptiveBackhaulTarget> {
        val out = mutableListOf<AdaptiveBackhaulTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveBackhaulTargetStore(): AdaptiveBackhaulTargetStore {
    return MemoryAdaptiveBackhaulTargetStore(
        mutableMapOf<String, AdaptiveBackhaulTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveBackhaulTargetStore(
    var inner: AdaptiveBackhaulTargetStore,
    var cache: TtlCache<AdaptiveBackhaulTarget>,
    var hits: Int,
    var misses: Int,
) : AdaptiveBackhaulTargetStore {
    override fun find(code: String): AdaptiveBackhaulTarget? {
        val hit = (cache.get(code) as AdaptiveBackhaulTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveBackhaulTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveBackhaulTarget> = inner.list()
}

fun newCachedAdaptiveBackhaulTargetStore(inner: AdaptiveBackhaulTargetStore, ttl: Int, clock: Clock): AdaptiveBackhaulTargetStore {
    return CachedAdaptiveBackhaulTargetStore(
        inner = inner,
        cache = newTtlCache<AdaptiveBackhaulTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive backhaul targets. Empty fields match all. */
data class AdaptiveBackhaulTargetQuery(
    var locale: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveBackhaulTarget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<AdaptiveBackhaulTarget>): MutableList<AdaptiveBackhaulTarget> {
        val matched = filter(items) { candidate: AdaptiveBackhaulTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveBackhaulTargets(store: AdaptiveBackhaulTargetStore, query: AdaptiveBackhaulTargetQuery): MutableList<AdaptiveBackhaulTarget> {
    return query.apply(store.list())
}
