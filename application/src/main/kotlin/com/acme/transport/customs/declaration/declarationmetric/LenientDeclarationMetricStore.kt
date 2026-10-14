package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient declaration metrics by code. */
interface LenientDeclarationMetricStore {
    fun find(code: String): LenientDeclarationMetric?
    fun save(item: LenientDeclarationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDeclarationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDeclarationMetricStore(
    var items: MutableMap<String, LenientDeclarationMetric>,
    var order: MutableList<String>,
) : LenientDeclarationMetricStore {
    override fun find(code: String): LenientDeclarationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDeclarationMetric) {
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

    override fun list(): MutableList<LenientDeclarationMetric> {
        val out = mutableListOf<LenientDeclarationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDeclarationMetricStore(): LenientDeclarationMetricStore {
    return MemoryLenientDeclarationMetricStore(
        mutableMapOf<String, LenientDeclarationMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDeclarationMetricStore(
    var inner: LenientDeclarationMetricStore,
    var cache: TtlCache<LenientDeclarationMetric>,
    var hits: Int,
    var misses: Int,
) : LenientDeclarationMetricStore {
    override fun find(code: String): LenientDeclarationMetric? {
        val hit = (cache.get(code) as LenientDeclarationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDeclarationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDeclarationMetric> = inner.list()
}

fun newCachedLenientDeclarationMetricStore(inner: LenientDeclarationMetricStore, ttl: Int, clock: Clock): LenientDeclarationMetricStore {
    return CachedLenientDeclarationMetricStore(
        inner = inner,
        cache = newTtlCache<LenientDeclarationMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient declaration metrics. Empty fields match all. */
data class LenientDeclarationMetricQuery(
    var country: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDeclarationMetric): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<LenientDeclarationMetric>): MutableList<LenientDeclarationMetric> {
        val matched = filter(items) { candidate: LenientDeclarationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDeclarationMetrics(store: LenientDeclarationMetricStore, query: LenientDeclarationMetricQuery): MutableList<LenientDeclarationMetric> {
    return query.apply(store.list())
}
