package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive trailer baselines by code. */
interface AdaptiveTrailerBaselineStore {
    fun find(code: String): AdaptiveTrailerBaseline?
    fun save(item: AdaptiveTrailerBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTrailerBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTrailerBaselineStore(
    var items: MutableMap<String, AdaptiveTrailerBaseline>,
    var order: MutableList<String>,
) : AdaptiveTrailerBaselineStore {
    override fun find(code: String): AdaptiveTrailerBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTrailerBaseline) {
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

    override fun list(): MutableList<AdaptiveTrailerBaseline> {
        val out = mutableListOf<AdaptiveTrailerBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTrailerBaselineStore(): AdaptiveTrailerBaselineStore {
    return MemoryAdaptiveTrailerBaselineStore(
        mutableMapOf<String, AdaptiveTrailerBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTrailerBaselineStore(
    var inner: AdaptiveTrailerBaselineStore,
    var cache: TtlCache<AdaptiveTrailerBaseline>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTrailerBaselineStore {
    override fun find(code: String): AdaptiveTrailerBaseline? {
        val hit = (cache.get(code) as AdaptiveTrailerBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTrailerBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTrailerBaseline> = inner.list()
}

fun newCachedAdaptiveTrailerBaselineStore(inner: AdaptiveTrailerBaselineStore, ttl: Int, clock: Clock): AdaptiveTrailerBaselineStore {
    return CachedAdaptiveTrailerBaselineStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTrailerBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive trailer baselines. Empty fields match all. */
data class AdaptiveTrailerBaselineQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTrailerBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTrailerBaseline>): MutableList<AdaptiveTrailerBaseline> {
        val matched = filter(items) { candidate: AdaptiveTrailerBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTrailerBaselines(store: AdaptiveTrailerBaselineStore, query: AdaptiveTrailerBaselineQuery): MutableList<AdaptiveTrailerBaseline> {
    return query.apply(store.list())
}
