package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer targets by code. */
interface TrailerTargetStore {
    fun find(code: String): TrailerTarget?
    fun save(item: TrailerTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerTargetStore(
    var items: MutableMap<String, TrailerTarget>,
    var order: MutableList<String>,
) : TrailerTargetStore {
    override fun find(code: String): TrailerTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerTarget) {
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

    override fun list(): MutableList<TrailerTarget> {
        val out = mutableListOf<TrailerTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerTargetStore(): TrailerTargetStore {
    return MemoryTrailerTargetStore(mutableMapOf<String, TrailerTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerTargetStore(
    var inner: TrailerTargetStore,
    var cache: TtlCache<TrailerTarget>,
    var hits: Int,
    var misses: Int,
) : TrailerTargetStore {
    override fun find(code: String): TrailerTarget? {
        val hit = (cache.get(code) as TrailerTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerTarget> = inner.list()
}

fun newCachedTrailerTargetStore(inner: TrailerTargetStore, ttl: Int, clock: Clock): TrailerTargetStore {
    return CachedTrailerTargetStore(
        inner = inner,
        cache = newTtlCache<TrailerTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer targets. Empty fields match all. */
data class TrailerTargetQuery(
    var region: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerTarget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<TrailerTarget>): MutableList<TrailerTarget> {
        val matched = filter(items) { candidate: TrailerTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerTargets(store: TrailerTargetStore, query: TrailerTargetQuery): MutableList<TrailerTarget> {
    return query.apply(store.list())
}
