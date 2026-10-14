package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone cycles by code. */
interface ZoneCycleStore {
    fun find(code: String): ZoneCycle?
    fun save(item: ZoneCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneCycleStore(
    var items: MutableMap<String, ZoneCycle>,
    var order: MutableList<String>,
) : ZoneCycleStore {
    override fun find(code: String): ZoneCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneCycle) {
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

    override fun list(): MutableList<ZoneCycle> {
        val out = mutableListOf<ZoneCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneCycleStore(): ZoneCycleStore {
    return MemoryZoneCycleStore(mutableMapOf<String, ZoneCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneCycleStore(
    var inner: ZoneCycleStore,
    var cache: TtlCache<ZoneCycle>,
    var hits: Int,
    var misses: Int,
) : ZoneCycleStore {
    override fun find(code: String): ZoneCycle? {
        val hit = (cache.get(code) as ZoneCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneCycle> = inner.list()
}

fun newCachedZoneCycleStore(inner: ZoneCycleStore, ttl: Int, clock: Clock): ZoneCycleStore {
    return CachedZoneCycleStore(
        inner = inner,
        cache = newTtlCache<ZoneCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone cycles. Empty fields match all. */
data class ZoneCycleQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ZoneCycle>): MutableList<ZoneCycle> {
        val matched = filter(items) { candidate: ZoneCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneCycles(store: ZoneCycleStore, query: ZoneCycleQuery): MutableList<ZoneCycle> {
    return query.apply(store.list())
}
