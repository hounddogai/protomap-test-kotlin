package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending node rollups by code. */
interface PendingNodeRollupStore {
    fun find(code: String): PendingNodeRollup?
    fun save(item: PendingNodeRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNodeRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNodeRollupStore(
    var items: MutableMap<String, PendingNodeRollup>,
    var order: MutableList<String>,
) : PendingNodeRollupStore {
    override fun find(code: String): PendingNodeRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNodeRollup) {
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

    override fun list(): MutableList<PendingNodeRollup> {
        val out = mutableListOf<PendingNodeRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNodeRollupStore(): PendingNodeRollupStore {
    return MemoryPendingNodeRollupStore(mutableMapOf<String, PendingNodeRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNodeRollupStore(
    var inner: PendingNodeRollupStore,
    var cache: TtlCache<PendingNodeRollup>,
    var hits: Int,
    var misses: Int,
) : PendingNodeRollupStore {
    override fun find(code: String): PendingNodeRollup? {
        val hit = (cache.get(code) as PendingNodeRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNodeRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNodeRollup> = inner.list()
}

fun newCachedPendingNodeRollupStore(inner: PendingNodeRollupStore, ttl: Int, clock: Clock): PendingNodeRollupStore {
    return CachedPendingNodeRollupStore(
        inner = inner,
        cache = newTtlCache<PendingNodeRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending node rollups. Empty fields match all. */
data class PendingNodeRollupQuery(
    var region: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNodeRollup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingNodeRollup>): MutableList<PendingNodeRollup> {
        val matched = filter(items) { candidate: PendingNodeRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNodeRollups(store: PendingNodeRollupStore, query: PendingNodeRollupQuery): MutableList<PendingNodeRollup> {
    return query.apply(store.list())
}
