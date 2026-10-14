package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending cutoff rollups by code. */
interface PendingCutoffRollupStore {
    fun find(code: String): PendingCutoffRollup?
    fun save(item: PendingCutoffRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCutoffRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCutoffRollupStore(
    var items: MutableMap<String, PendingCutoffRollup>,
    var order: MutableList<String>,
) : PendingCutoffRollupStore {
    override fun find(code: String): PendingCutoffRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCutoffRollup) {
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

    override fun list(): MutableList<PendingCutoffRollup> {
        val out = mutableListOf<PendingCutoffRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCutoffRollupStore(): PendingCutoffRollupStore {
    return MemoryPendingCutoffRollupStore(mutableMapOf<String, PendingCutoffRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCutoffRollupStore(
    var inner: PendingCutoffRollupStore,
    var cache: TtlCache<PendingCutoffRollup>,
    var hits: Int,
    var misses: Int,
) : PendingCutoffRollupStore {
    override fun find(code: String): PendingCutoffRollup? {
        val hit = (cache.get(code) as PendingCutoffRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCutoffRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCutoffRollup> = inner.list()
}

fun newCachedPendingCutoffRollupStore(inner: PendingCutoffRollupStore, ttl: Int, clock: Clock): PendingCutoffRollupStore {
    return CachedPendingCutoffRollupStore(
        inner = inner,
        cache = newTtlCache<PendingCutoffRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending cutoff rollups. Empty fields match all. */
data class PendingCutoffRollupQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCutoffRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCutoffRollup>): MutableList<PendingCutoffRollup> {
        val matched = filter(items) { candidate: PendingCutoffRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCutoffRollups(store: PendingCutoffRollupStore, query: PendingCutoffRollupQuery): MutableList<PendingCutoffRollup> {
    return query.apply(store.list())
}
