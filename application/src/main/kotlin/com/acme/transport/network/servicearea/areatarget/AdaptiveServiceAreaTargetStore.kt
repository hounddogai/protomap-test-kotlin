package com.acme.transport.network.servicearea.areatarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive service area targets by code. */
interface AdaptiveServiceAreaTargetStore {
    fun find(code: String): AdaptiveServiceAreaTarget?
    fun save(item: AdaptiveServiceAreaTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveServiceAreaTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveServiceAreaTargetStore(
    var items: MutableMap<String, AdaptiveServiceAreaTarget>,
    var order: MutableList<String>,
) : AdaptiveServiceAreaTargetStore {
    override fun find(code: String): AdaptiveServiceAreaTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveServiceAreaTarget) {
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

    override fun list(): MutableList<AdaptiveServiceAreaTarget> {
        val out = mutableListOf<AdaptiveServiceAreaTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveServiceAreaTargetStore(): AdaptiveServiceAreaTargetStore {
    return MemoryAdaptiveServiceAreaTargetStore(
        mutableMapOf<String, AdaptiveServiceAreaTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveServiceAreaTargetStore(
    var inner: AdaptiveServiceAreaTargetStore,
    var cache: TtlCache<AdaptiveServiceAreaTarget>,
    var hits: Int,
    var misses: Int,
) : AdaptiveServiceAreaTargetStore {
    override fun find(code: String): AdaptiveServiceAreaTarget? {
        val hit = (cache.get(code) as AdaptiveServiceAreaTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveServiceAreaTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveServiceAreaTarget> = inner.list()
}

fun newCachedAdaptiveServiceAreaTargetStore(inner: AdaptiveServiceAreaTargetStore, ttl: Int, clock: Clock): AdaptiveServiceAreaTargetStore {
    return CachedAdaptiveServiceAreaTargetStore(
        inner = inner,
        cache = newTtlCache<AdaptiveServiceAreaTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive service area targets. Empty fields match all. */
data class AdaptiveServiceAreaTargetQuery(
    var locale: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveServiceAreaTarget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveServiceAreaTarget>): MutableList<AdaptiveServiceAreaTarget> {
        val matched = filter(items) { candidate: AdaptiveServiceAreaTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveServiceAreaTargets(store: AdaptiveServiceAreaTargetStore, query: AdaptiveServiceAreaTargetQuery): MutableList<AdaptiveServiceAreaTarget> {
    return query.apply(store.list())
}
