package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock overrides by code. */
interface CrossDockOverrideStore {
    fun find(code: String): CrossDockOverride?
    fun save(item: CrossDockOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockOverrideStore(
    var items: MutableMap<String, CrossDockOverride>,
    var order: MutableList<String>,
) : CrossDockOverrideStore {
    override fun find(code: String): CrossDockOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockOverride) {
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

    override fun list(): MutableList<CrossDockOverride> {
        val out = mutableListOf<CrossDockOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockOverrideStore(): CrossDockOverrideStore {
    return MemoryCrossDockOverrideStore(mutableMapOf<String, CrossDockOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockOverrideStore(
    var inner: CrossDockOverrideStore,
    var cache: TtlCache<CrossDockOverride>,
    var hits: Int,
    var misses: Int,
) : CrossDockOverrideStore {
    override fun find(code: String): CrossDockOverride? {
        val hit = (cache.get(code) as CrossDockOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockOverride> = inner.list()
}

fun newCachedCrossDockOverrideStore(inner: CrossDockOverrideStore, ttl: Int, clock: Clock): CrossDockOverrideStore {
    return CachedCrossDockOverrideStore(
        inner = inner,
        cache = newTtlCache<CrossDockOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock overrides. Empty fields match all. */
data class CrossDockOverrideQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockOverride): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockOverride>): MutableList<CrossDockOverride> {
        val matched = filter(items) { candidate: CrossDockOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockOverrides(store: CrossDockOverrideStore, query: CrossDockOverrideQuery): MutableList<CrossDockOverride> {
    return query.apply(store.list())
}
