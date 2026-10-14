package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default attempt metrics by code. */
interface DefaultAttemptMetricStore {
    fun find(code: String): DefaultAttemptMetric?
    fun save(item: DefaultAttemptMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAttemptMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAttemptMetricStore(
    var items: MutableMap<String, DefaultAttemptMetric>,
    var order: MutableList<String>,
) : DefaultAttemptMetricStore {
    override fun find(code: String): DefaultAttemptMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAttemptMetric) {
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

    override fun list(): MutableList<DefaultAttemptMetric> {
        val out = mutableListOf<DefaultAttemptMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAttemptMetricStore(): DefaultAttemptMetricStore {
    return MemoryDefaultAttemptMetricStore(
        mutableMapOf<String, DefaultAttemptMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAttemptMetricStore(
    var inner: DefaultAttemptMetricStore,
    var cache: TtlCache<DefaultAttemptMetric>,
    var hits: Int,
    var misses: Int,
) : DefaultAttemptMetricStore {
    override fun find(code: String): DefaultAttemptMetric? {
        val hit = (cache.get(code) as DefaultAttemptMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAttemptMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAttemptMetric> = inner.list()
}

fun newCachedDefaultAttemptMetricStore(inner: DefaultAttemptMetricStore, ttl: Int, clock: Clock): DefaultAttemptMetricStore {
    return CachedDefaultAttemptMetricStore(
        inner = inner,
        cache = newTtlCache<DefaultAttemptMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default attempt metrics. Empty fields match all. */
data class DefaultAttemptMetricQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAttemptMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<DefaultAttemptMetric>): MutableList<DefaultAttemptMetric> {
        val matched = filter(items) { candidate: DefaultAttemptMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAttemptMetrics(store: DefaultAttemptMetricStore, query: DefaultAttemptMetricQuery): MutableList<DefaultAttemptMetric> {
    return query.apply(store.list())
}
