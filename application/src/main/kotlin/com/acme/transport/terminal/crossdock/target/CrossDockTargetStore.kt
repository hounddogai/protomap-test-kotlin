package com.acme.transport.terminal.crossdock.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock targets by code. */
interface CrossDockTargetStore {
    fun find(code: String): CrossDockTarget?
    fun save(item: CrossDockTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockTargetStore(
    var items: MutableMap<String, CrossDockTarget>,
    var order: MutableList<String>,
) : CrossDockTargetStore {
    override fun find(code: String): CrossDockTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockTarget) {
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

    override fun list(): MutableList<CrossDockTarget> {
        val out = mutableListOf<CrossDockTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockTargetStore(): CrossDockTargetStore {
    return MemoryCrossDockTargetStore(mutableMapOf<String, CrossDockTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockTargetStore(
    var inner: CrossDockTargetStore,
    var cache: TtlCache<CrossDockTarget>,
    var hits: Int,
    var misses: Int,
) : CrossDockTargetStore {
    override fun find(code: String): CrossDockTarget? {
        val hit = (cache.get(code) as CrossDockTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockTarget> = inner.list()
}

fun newCachedCrossDockTargetStore(inner: CrossDockTargetStore, ttl: Int, clock: Clock): CrossDockTargetStore {
    return CachedCrossDockTargetStore(
        inner = inner,
        cache = newTtlCache<CrossDockTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock targets. Empty fields match all. */
data class CrossDockTargetQuery(
    var depot: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockTarget>): MutableList<CrossDockTarget> {
        val matched = filter(items) { candidate: CrossDockTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockTargets(store: CrossDockTargetStore, query: CrossDockTargetQuery): MutableList<CrossDockTarget> {
    return query.apply(store.list())
}
