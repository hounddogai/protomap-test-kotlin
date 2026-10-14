package com.acme.transport.crewing.swap.score

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap snapshots by code. */
interface SwapSnapshotStore {
    fun find(code: String): SwapSnapshot?
    fun save(item: SwapSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapSnapshotStore(
    var items: MutableMap<String, SwapSnapshot>,
    var order: MutableList<String>,
) : SwapSnapshotStore {
    override fun find(code: String): SwapSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapSnapshot) {
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

    override fun list(): MutableList<SwapSnapshot> {
        val out = mutableListOf<SwapSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapSnapshotStore(): SwapSnapshotStore {
    return MemorySwapSnapshotStore(mutableMapOf<String, SwapSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapSnapshotStore(
    var inner: SwapSnapshotStore,
    var cache: TtlCache<SwapSnapshot>,
    var hits: Int,
    var misses: Int,
) : SwapSnapshotStore {
    override fun find(code: String): SwapSnapshot? {
        val hit = (cache.get(code) as SwapSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapSnapshot> = inner.list()
}

fun newCachedSwapSnapshotStore(inner: SwapSnapshotStore, ttl: Int, clock: Clock): SwapSnapshotStore {
    return CachedSwapSnapshotStore(
        inner = inner,
        cache = newTtlCache<SwapSnapshot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap snapshots. Empty fields match all. */
data class SwapSnapshotQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapSnapshot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapSnapshot>): MutableList<SwapSnapshot> {
        val matched = filter(items) { candidate: SwapSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapSnapshots(store: SwapSnapshotStore, query: SwapSnapshotQuery): MutableList<SwapSnapshot> {
    return query.apply(store.list())
}
