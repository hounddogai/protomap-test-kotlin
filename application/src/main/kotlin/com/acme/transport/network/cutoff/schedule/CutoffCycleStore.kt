package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff cycles by code. */
interface CutoffCycleStore {
    fun find(code: String): CutoffCycle?
    fun save(item: CutoffCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffCycleStore(
    var items: MutableMap<String, CutoffCycle>,
    var order: MutableList<String>,
) : CutoffCycleStore {
    override fun find(code: String): CutoffCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffCycle) {
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

    override fun list(): MutableList<CutoffCycle> {
        val out = mutableListOf<CutoffCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffCycleStore(): CutoffCycleStore {
    return MemoryCutoffCycleStore(mutableMapOf<String, CutoffCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffCycleStore(
    var inner: CutoffCycleStore,
    var cache: TtlCache<CutoffCycle>,
    var hits: Int,
    var misses: Int,
) : CutoffCycleStore {
    override fun find(code: String): CutoffCycle? {
        val hit = (cache.get(code) as CutoffCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffCycle> = inner.list()
}

fun newCachedCutoffCycleStore(inner: CutoffCycleStore, ttl: Int, clock: Clock): CutoffCycleStore {
    return CachedCutoffCycleStore(
        inner = inner,
        cache = newTtlCache<CutoffCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff cycles. Empty fields match all. */
data class CutoffCycleQuery(
    var locale: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffCycle): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffCycle>): MutableList<CutoffCycle> {
        val matched = filter(items) { candidate: CutoffCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffCycles(store: CutoffCycleStore, query: CutoffCycleQuery): MutableList<CutoffCycle> {
    return query.apply(store.list())
}
