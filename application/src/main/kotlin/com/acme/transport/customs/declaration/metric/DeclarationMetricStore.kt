package com.acme.transport.customs.declaration.metric

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration metrics by code. */
interface DeclarationMetricStore {
    fun find(code: String): DeclarationMetric?
    fun save(item: DeclarationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationMetricStore(
    var items: MutableMap<String, DeclarationMetric>,
    var order: MutableList<String>,
) : DeclarationMetricStore {
    override fun find(code: String): DeclarationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationMetric) {
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

    override fun list(): MutableList<DeclarationMetric> {
        val out = mutableListOf<DeclarationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationMetricStore(): DeclarationMetricStore {
    return MemoryDeclarationMetricStore(mutableMapOf<String, DeclarationMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationMetricStore(
    var inner: DeclarationMetricStore,
    var cache: TtlCache<DeclarationMetric>,
    var hits: Int,
    var misses: Int,
) : DeclarationMetricStore {
    override fun find(code: String): DeclarationMetric? {
        val hit = (cache.get(code) as DeclarationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationMetric> = inner.list()
}

fun newCachedDeclarationMetricStore(inner: DeclarationMetricStore, ttl: Int, clock: Clock): DeclarationMetricStore {
    return CachedDeclarationMetricStore(
        inner = inner,
        cache = newTtlCache<DeclarationMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration metrics. Empty fields match all. */
data class DeclarationMetricQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationMetric>): MutableList<DeclarationMetric> {
        val matched = filter(items) { candidate: DeclarationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationMetrics(store: DeclarationMetricStore, query: DeclarationMetricQuery): MutableList<DeclarationMetric> {
    return query.apply(store.list())
}
