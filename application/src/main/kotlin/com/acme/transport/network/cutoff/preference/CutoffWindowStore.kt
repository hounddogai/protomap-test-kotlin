package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff windows by code. */
interface CutoffWindowStore {
    fun find(code: String): CutoffWindow?
    fun save(item: CutoffWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffWindowStore(
    var items: MutableMap<String, CutoffWindow>,
    var order: MutableList<String>,
) : CutoffWindowStore {
    override fun find(code: String): CutoffWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffWindow) {
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

    override fun list(): MutableList<CutoffWindow> {
        val out = mutableListOf<CutoffWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffWindowStore(): CutoffWindowStore {
    return MemoryCutoffWindowStore(mutableMapOf<String, CutoffWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffWindowStore(
    var inner: CutoffWindowStore,
    var cache: TtlCache<CutoffWindow>,
    var hits: Int,
    var misses: Int,
) : CutoffWindowStore {
    override fun find(code: String): CutoffWindow? {
        val hit = (cache.get(code) as CutoffWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffWindow> = inner.list()
}

fun newCachedCutoffWindowStore(inner: CutoffWindowStore, ttl: Int, clock: Clock): CutoffWindowStore {
    return CachedCutoffWindowStore(
        inner = inner,
        cache = newTtlCache<CutoffWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff windows. Empty fields match all. */
data class CutoffWindowQuery(
    var lane: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffWindow): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffWindow>): MutableList<CutoffWindow> {
        val matched = filter(items) { candidate: CutoffWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffWindows(store: CutoffWindowStore, query: CutoffWindowQuery): MutableList<CutoffWindow> {
    return query.apply(store.list())
}
