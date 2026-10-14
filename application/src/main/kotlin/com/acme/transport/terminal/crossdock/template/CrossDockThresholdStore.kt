package com.acme.transport.terminal.crossdock.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock thresholds by code. */
interface CrossDockThresholdStore {
    fun find(code: String): CrossDockThreshold?
    fun save(item: CrossDockThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockThresholdStore(
    var items: MutableMap<String, CrossDockThreshold>,
    var order: MutableList<String>,
) : CrossDockThresholdStore {
    override fun find(code: String): CrossDockThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockThreshold) {
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

    override fun list(): MutableList<CrossDockThreshold> {
        val out = mutableListOf<CrossDockThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockThresholdStore(): CrossDockThresholdStore {
    return MemoryCrossDockThresholdStore(mutableMapOf<String, CrossDockThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockThresholdStore(
    var inner: CrossDockThresholdStore,
    var cache: TtlCache<CrossDockThreshold>,
    var hits: Int,
    var misses: Int,
) : CrossDockThresholdStore {
    override fun find(code: String): CrossDockThreshold? {
        val hit = (cache.get(code) as CrossDockThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockThreshold> = inner.list()
}

fun newCachedCrossDockThresholdStore(inner: CrossDockThresholdStore, ttl: Int, clock: Clock): CrossDockThresholdStore {
    return CachedCrossDockThresholdStore(
        inner = inner,
        cache = newTtlCache<CrossDockThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock thresholds. Empty fields match all. */
data class CrossDockThresholdQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockThreshold>): MutableList<CrossDockThreshold> {
        val matched = filter(items) { candidate: CrossDockThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockThresholds(store: CrossDockThresholdStore, query: CrossDockThresholdQuery): MutableList<CrossDockThreshold> {
    return query.apply(store.list())
}
