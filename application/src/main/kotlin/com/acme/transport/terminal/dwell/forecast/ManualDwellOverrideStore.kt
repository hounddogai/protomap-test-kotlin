package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual dwell overrides by code. */
interface ManualDwellOverrideStore {
    fun find(code: String): ManualDwellOverride?
    fun save(item: ManualDwellOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDwellOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDwellOverrideStore(
    var items: MutableMap<String, ManualDwellOverride>,
    var order: MutableList<String>,
) : ManualDwellOverrideStore {
    override fun find(code: String): ManualDwellOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDwellOverride) {
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

    override fun list(): MutableList<ManualDwellOverride> {
        val out = mutableListOf<ManualDwellOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDwellOverrideStore(): ManualDwellOverrideStore {
    return MemoryManualDwellOverrideStore(mutableMapOf<String, ManualDwellOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDwellOverrideStore(
    var inner: ManualDwellOverrideStore,
    var cache: TtlCache<ManualDwellOverride>,
    var hits: Int,
    var misses: Int,
) : ManualDwellOverrideStore {
    override fun find(code: String): ManualDwellOverride? {
        val hit = (cache.get(code) as ManualDwellOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDwellOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDwellOverride> = inner.list()
}

fun newCachedManualDwellOverrideStore(inner: ManualDwellOverrideStore, ttl: Int, clock: Clock): ManualDwellOverrideStore {
    return CachedManualDwellOverrideStore(
        inner = inner,
        cache = newTtlCache<ManualDwellOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual dwell overrides. Empty fields match all. */
data class ManualDwellOverrideQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDwellOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDwellOverride>): MutableList<ManualDwellOverride> {
        val matched = filter(items) { candidate: ManualDwellOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDwellOverrides(store: ManualDwellOverrideStore, query: ManualDwellOverrideQuery): MutableList<ManualDwellOverride> {
    return query.apply(store.list())
}
