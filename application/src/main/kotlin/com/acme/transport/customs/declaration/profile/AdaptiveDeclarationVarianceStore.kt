package com.acme.transport.customs.declaration.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive declaration variances by code. */
interface AdaptiveDeclarationVarianceStore {
    fun find(code: String): AdaptiveDeclarationVariance?
    fun save(item: AdaptiveDeclarationVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDeclarationVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDeclarationVarianceStore(
    var items: MutableMap<String, AdaptiveDeclarationVariance>,
    var order: MutableList<String>,
) : AdaptiveDeclarationVarianceStore {
    override fun find(code: String): AdaptiveDeclarationVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDeclarationVariance) {
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

    override fun list(): MutableList<AdaptiveDeclarationVariance> {
        val out = mutableListOf<AdaptiveDeclarationVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDeclarationVarianceStore(): AdaptiveDeclarationVarianceStore {
    return MemoryAdaptiveDeclarationVarianceStore(
        mutableMapOf<String, AdaptiveDeclarationVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDeclarationVarianceStore(
    var inner: AdaptiveDeclarationVarianceStore,
    var cache: TtlCache<AdaptiveDeclarationVariance>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDeclarationVarianceStore {
    override fun find(code: String): AdaptiveDeclarationVariance? {
        val hit = (cache.get(code) as AdaptiveDeclarationVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDeclarationVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDeclarationVariance> = inner.list()
}

fun newCachedAdaptiveDeclarationVarianceStore(inner: AdaptiveDeclarationVarianceStore, ttl: Int, clock: Clock): AdaptiveDeclarationVarianceStore {
    return CachedAdaptiveDeclarationVarianceStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDeclarationVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive declaration variances. Empty fields match all. */
data class AdaptiveDeclarationVarianceQuery(
    var locale: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDeclarationVariance): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDeclarationVariance>): MutableList<AdaptiveDeclarationVariance> {
        val matched = filter(items) { candidate: AdaptiveDeclarationVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDeclarationVariances(store: AdaptiveDeclarationVarianceStore, query: AdaptiveDeclarationVarianceQuery): MutableList<AdaptiveDeclarationVariance> {
    return query.apply(store.list())
}
