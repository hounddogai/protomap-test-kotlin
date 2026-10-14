package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock cycles by code. */
interface CrossDockCycleStore {
    fun find(code: String): CrossDockCycle?
    fun save(item: CrossDockCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockCycleStore(
    var items: MutableMap<String, CrossDockCycle>,
    var order: MutableList<String>,
) : CrossDockCycleStore {
    override fun find(code: String): CrossDockCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockCycle) {
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

    override fun list(): MutableList<CrossDockCycle> {
        val out = mutableListOf<CrossDockCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockCycleStore(): CrossDockCycleStore {
    return MemoryCrossDockCycleStore(mutableMapOf<String, CrossDockCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockCycleStore(
    var inner: CrossDockCycleStore,
    var cache: TtlCache<CrossDockCycle>,
    var hits: Int,
    var misses: Int,
) : CrossDockCycleStore {
    override fun find(code: String): CrossDockCycle? {
        val hit = (cache.get(code) as CrossDockCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockCycle> = inner.list()
}

fun newCachedCrossDockCycleStore(inner: CrossDockCycleStore, ttl: Int, clock: Clock): CrossDockCycleStore {
    return CachedCrossDockCycleStore(
        inner = inner,
        cache = newTtlCache<CrossDockCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock cycles. Empty fields match all. */
data class CrossDockCycleQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockCycle>): MutableList<CrossDockCycle> {
        val matched = filter(items) { candidate: CrossDockCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockCycles(store: CrossDockCycleStore, query: CrossDockCycleQuery): MutableList<CrossDockCycle> {
    return query.apply(store.list())
}
