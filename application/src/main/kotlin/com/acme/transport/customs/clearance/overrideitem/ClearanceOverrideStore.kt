package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance overrides by code. */
interface ClearanceOverrideStore {
    fun find(code: String): ClearanceOverride?
    fun save(item: ClearanceOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceOverrideStore(
    var items: MutableMap<String, ClearanceOverride>,
    var order: MutableList<String>,
) : ClearanceOverrideStore {
    override fun find(code: String): ClearanceOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceOverride) {
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

    override fun list(): MutableList<ClearanceOverride> {
        val out = mutableListOf<ClearanceOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceOverrideStore(): ClearanceOverrideStore {
    return MemoryClearanceOverrideStore(mutableMapOf<String, ClearanceOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceOverrideStore(
    var inner: ClearanceOverrideStore,
    var cache: TtlCache<ClearanceOverride>,
    var hits: Int,
    var misses: Int,
) : ClearanceOverrideStore {
    override fun find(code: String): ClearanceOverride? {
        val hit = (cache.get(code) as ClearanceOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceOverride> = inner.list()
}

fun newCachedClearanceOverrideStore(inner: ClearanceOverrideStore, ttl: Int, clock: Clock): ClearanceOverrideStore {
    return CachedClearanceOverrideStore(
        inner = inner,
        cache = newTtlCache<ClearanceOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance overrides. Empty fields match all. */
data class ClearanceOverrideQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ClearanceOverride>): MutableList<ClearanceOverride> {
        val matched = filter(items) { candidate: ClearanceOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceOverrides(store: ClearanceOverrideStore, query: ClearanceOverrideQuery): MutableList<ClearanceOverride> {
    return query.apply(store.list())
}
