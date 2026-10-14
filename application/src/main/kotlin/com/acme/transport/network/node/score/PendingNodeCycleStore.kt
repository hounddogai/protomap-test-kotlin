package com.acme.transport.network.node.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending node cycles by code. */
interface PendingNodeCycleStore {
    fun find(code: String): PendingNodeCycle?
    fun save(item: PendingNodeCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNodeCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNodeCycleStore(
    var items: MutableMap<String, PendingNodeCycle>,
    var order: MutableList<String>,
) : PendingNodeCycleStore {
    override fun find(code: String): PendingNodeCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNodeCycle) {
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

    override fun list(): MutableList<PendingNodeCycle> {
        val out = mutableListOf<PendingNodeCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNodeCycleStore(): PendingNodeCycleStore {
    return MemoryPendingNodeCycleStore(mutableMapOf<String, PendingNodeCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNodeCycleStore(
    var inner: PendingNodeCycleStore,
    var cache: TtlCache<PendingNodeCycle>,
    var hits: Int,
    var misses: Int,
) : PendingNodeCycleStore {
    override fun find(code: String): PendingNodeCycle? {
        val hit = (cache.get(code) as PendingNodeCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNodeCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNodeCycle> = inner.list()
}

fun newCachedPendingNodeCycleStore(inner: PendingNodeCycleStore, ttl: Int, clock: Clock): PendingNodeCycleStore {
    return CachedPendingNodeCycleStore(
        inner = inner,
        cache = newTtlCache<PendingNodeCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending node cycles. Empty fields match all. */
data class PendingNodeCycleQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNodeCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<PendingNodeCycle>): MutableList<PendingNodeCycle> {
        val matched = filter(items) { candidate: PendingNodeCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNodeCycles(store: PendingNodeCycleStore, query: PendingNodeCycleQuery): MutableList<PendingNodeCycle> {
    return query.apply(store.list())
}
