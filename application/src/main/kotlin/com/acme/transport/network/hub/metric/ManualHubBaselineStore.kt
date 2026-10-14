package com.acme.transport.network.hub.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual hub baselines by code. */
interface ManualHubBaselineStore {
    fun find(code: String): ManualHubBaseline?
    fun save(item: ManualHubBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualHubBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualHubBaselineStore(
    var items: MutableMap<String, ManualHubBaseline>,
    var order: MutableList<String>,
) : ManualHubBaselineStore {
    override fun find(code: String): ManualHubBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualHubBaseline) {
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

    override fun list(): MutableList<ManualHubBaseline> {
        val out = mutableListOf<ManualHubBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualHubBaselineStore(): ManualHubBaselineStore {
    return MemoryManualHubBaselineStore(mutableMapOf<String, ManualHubBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualHubBaselineStore(
    var inner: ManualHubBaselineStore,
    var cache: TtlCache<ManualHubBaseline>,
    var hits: Int,
    var misses: Int,
) : ManualHubBaselineStore {
    override fun find(code: String): ManualHubBaseline? {
        val hit = (cache.get(code) as ManualHubBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualHubBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualHubBaseline> = inner.list()
}

fun newCachedManualHubBaselineStore(inner: ManualHubBaselineStore, ttl: Int, clock: Clock): ManualHubBaselineStore {
    return CachedManualHubBaselineStore(
        inner = inner,
        cache = newTtlCache<ManualHubBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual hub baselines. Empty fields match all. */
data class ManualHubBaselineQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualHubBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ManualHubBaseline>): MutableList<ManualHubBaseline> {
        val matched = filter(items) { candidate: ManualHubBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualHubBaselines(store: ManualHubBaselineStore, query: ManualHubBaselineQuery): MutableList<ManualHubBaseline> {
    return query.apply(store.list())
}
