package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped hub baselines by code. */
interface CappedHubBaselineStore {
    fun find(code: String): CappedHubBaseline?
    fun save(item: CappedHubBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedHubBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedHubBaselineStore(
    var items: MutableMap<String, CappedHubBaseline>,
    var order: MutableList<String>,
) : CappedHubBaselineStore {
    override fun find(code: String): CappedHubBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedHubBaseline) {
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

    override fun list(): MutableList<CappedHubBaseline> {
        val out = mutableListOf<CappedHubBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedHubBaselineStore(): CappedHubBaselineStore {
    return MemoryCappedHubBaselineStore(mutableMapOf<String, CappedHubBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedHubBaselineStore(
    var inner: CappedHubBaselineStore,
    var cache: TtlCache<CappedHubBaseline>,
    var hits: Int,
    var misses: Int,
) : CappedHubBaselineStore {
    override fun find(code: String): CappedHubBaseline? {
        val hit = (cache.get(code) as CappedHubBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedHubBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedHubBaseline> = inner.list()
}

fun newCachedCappedHubBaselineStore(inner: CappedHubBaselineStore, ttl: Int, clock: Clock): CappedHubBaselineStore {
    return CachedCappedHubBaselineStore(
        inner = inner,
        cache = newTtlCache<CappedHubBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped hub baselines. Empty fields match all. */
data class CappedHubBaselineQuery(
    var source: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedHubBaseline): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedHubBaseline>): MutableList<CappedHubBaseline> {
        val matched = filter(items) { candidate: CappedHubBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedHubBaselines(store: CappedHubBaselineStore, query: CappedHubBaselineQuery): MutableList<CappedHubBaseline> {
    return query.apply(store.list())
}
