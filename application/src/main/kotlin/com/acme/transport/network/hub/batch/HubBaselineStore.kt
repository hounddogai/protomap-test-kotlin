package com.acme.transport.network.hub.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub baselines by code. */
interface HubBaselineStore {
    fun find(code: String): HubBaseline?
    fun save(item: HubBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubBaselineStore(
    var items: MutableMap<String, HubBaseline>,
    var order: MutableList<String>,
) : HubBaselineStore {
    override fun find(code: String): HubBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubBaseline) {
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

    override fun list(): MutableList<HubBaseline> {
        val out = mutableListOf<HubBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubBaselineStore(): HubBaselineStore {
    return MemoryHubBaselineStore(mutableMapOf<String, HubBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubBaselineStore(
    var inner: HubBaselineStore,
    var cache: TtlCache<HubBaseline>,
    var hits: Int,
    var misses: Int,
) : HubBaselineStore {
    override fun find(code: String): HubBaseline? {
        val hit = (cache.get(code) as HubBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubBaseline> = inner.list()
}

fun newCachedHubBaselineStore(inner: HubBaselineStore, ttl: Int, clock: Clock): HubBaselineStore {
    return CachedHubBaselineStore(
        inner = inner,
        cache = newTtlCache<HubBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub baselines. Empty fields match all. */
data class HubBaselineQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubBaseline>): MutableList<HubBaseline> {
        val matched = filter(items) { candidate: HubBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubBaselines(store: HubBaselineStore, query: HubBaselineQuery): MutableList<HubBaseline> {
    return query.apply(store.list())
}
