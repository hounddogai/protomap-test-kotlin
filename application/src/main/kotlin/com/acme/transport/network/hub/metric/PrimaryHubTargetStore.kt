package com.acme.transport.network.hub.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary hub targets by code. */
interface PrimaryHubTargetStore {
    fun find(code: String): PrimaryHubTarget?
    fun save(item: PrimaryHubTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryHubTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryHubTargetStore(
    var items: MutableMap<String, PrimaryHubTarget>,
    var order: MutableList<String>,
) : PrimaryHubTargetStore {
    override fun find(code: String): PrimaryHubTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryHubTarget) {
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

    override fun list(): MutableList<PrimaryHubTarget> {
        val out = mutableListOf<PrimaryHubTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryHubTargetStore(): PrimaryHubTargetStore {
    return MemoryPrimaryHubTargetStore(mutableMapOf<String, PrimaryHubTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryHubTargetStore(
    var inner: PrimaryHubTargetStore,
    var cache: TtlCache<PrimaryHubTarget>,
    var hits: Int,
    var misses: Int,
) : PrimaryHubTargetStore {
    override fun find(code: String): PrimaryHubTarget? {
        val hit = (cache.get(code) as PrimaryHubTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryHubTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryHubTarget> = inner.list()
}

fun newCachedPrimaryHubTargetStore(inner: PrimaryHubTargetStore, ttl: Int, clock: Clock): PrimaryHubTargetStore {
    return CachedPrimaryHubTargetStore(
        inner = inner,
        cache = newTtlCache<PrimaryHubTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary hub targets. Empty fields match all. */
data class PrimaryHubTargetQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryHubTarget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryHubTarget>): MutableList<PrimaryHubTarget> {
        val matched = filter(items) { candidate: PrimaryHubTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryHubTargets(store: PrimaryHubTargetStore, query: PrimaryHubTargetQuery): MutableList<PrimaryHubTarget> {
    return query.apply(store.list())
}
