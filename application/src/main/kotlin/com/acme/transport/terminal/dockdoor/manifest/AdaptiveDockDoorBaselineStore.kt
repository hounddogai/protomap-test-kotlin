package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive dock door baselines by code. */
interface AdaptiveDockDoorBaselineStore {
    fun find(code: String): AdaptiveDockDoorBaseline?
    fun save(item: AdaptiveDockDoorBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDockDoorBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDockDoorBaselineStore(
    var items: MutableMap<String, AdaptiveDockDoorBaseline>,
    var order: MutableList<String>,
) : AdaptiveDockDoorBaselineStore {
    override fun find(code: String): AdaptiveDockDoorBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDockDoorBaseline) {
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

    override fun list(): MutableList<AdaptiveDockDoorBaseline> {
        val out = mutableListOf<AdaptiveDockDoorBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDockDoorBaselineStore(): AdaptiveDockDoorBaselineStore {
    return MemoryAdaptiveDockDoorBaselineStore(
        mutableMapOf<String, AdaptiveDockDoorBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDockDoorBaselineStore(
    var inner: AdaptiveDockDoorBaselineStore,
    var cache: TtlCache<AdaptiveDockDoorBaseline>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDockDoorBaselineStore {
    override fun find(code: String): AdaptiveDockDoorBaseline? {
        val hit = (cache.get(code) as AdaptiveDockDoorBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDockDoorBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDockDoorBaseline> = inner.list()
}

fun newCachedAdaptiveDockDoorBaselineStore(inner: AdaptiveDockDoorBaselineStore, ttl: Int, clock: Clock): AdaptiveDockDoorBaselineStore {
    return CachedAdaptiveDockDoorBaselineStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDockDoorBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive dock door baselines. Empty fields match all. */
data class AdaptiveDockDoorBaselineQuery(
    var depot: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDockDoorBaseline): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<AdaptiveDockDoorBaseline>): MutableList<AdaptiveDockDoorBaseline> {
        val matched = filter(items) { candidate: AdaptiveDockDoorBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDockDoorBaselines(store: AdaptiveDockDoorBaselineStore, query: AdaptiveDockDoorBaselineQuery): MutableList<AdaptiveDockDoorBaseline> {
    return query.apply(store.list())
}
