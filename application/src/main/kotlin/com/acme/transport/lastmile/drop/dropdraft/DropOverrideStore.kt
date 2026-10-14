package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop overrides by code. */
interface DropOverrideStore {
    fun find(code: String): DropOverride?
    fun save(item: DropOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropOverrideStore(
    var items: MutableMap<String, DropOverride>,
    var order: MutableList<String>,
) : DropOverrideStore {
    override fun find(code: String): DropOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropOverride) {
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

    override fun list(): MutableList<DropOverride> {
        val out = mutableListOf<DropOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropOverrideStore(): DropOverrideStore {
    return MemoryDropOverrideStore(mutableMapOf<String, DropOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropOverrideStore(
    var inner: DropOverrideStore,
    var cache: TtlCache<DropOverride>,
    var hits: Int,
    var misses: Int,
) : DropOverrideStore {
    override fun find(code: String): DropOverride? {
        val hit = (cache.get(code) as DropOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropOverride> = inner.list()
}

fun newCachedDropOverrideStore(inner: DropOverrideStore, ttl: Int, clock: Clock): DropOverrideStore {
    return CachedDropOverrideStore(
        inner = inner,
        cache = newTtlCache<DropOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop overrides. Empty fields match all. */
data class DropOverrideQuery(
    var depot: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropOverride): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<DropOverride>): MutableList<DropOverride> {
        val matched = filter(items) { candidate: DropOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropOverrides(store: DropOverrideStore, query: DropOverrideQuery): MutableList<DropOverride> {
    return query.apply(store.list())
}
