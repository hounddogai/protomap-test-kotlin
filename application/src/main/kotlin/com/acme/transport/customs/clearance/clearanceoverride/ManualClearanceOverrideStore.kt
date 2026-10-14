package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual clearance overrides by code. */
interface ManualClearanceOverrideStore {
    fun find(code: String): ManualClearanceOverride?
    fun save(item: ManualClearanceOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualClearanceOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualClearanceOverrideStore(
    var items: MutableMap<String, ManualClearanceOverride>,
    var order: MutableList<String>,
) : ManualClearanceOverrideStore {
    override fun find(code: String): ManualClearanceOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualClearanceOverride) {
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

    override fun list(): MutableList<ManualClearanceOverride> {
        val out = mutableListOf<ManualClearanceOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualClearanceOverrideStore(): ManualClearanceOverrideStore {
    return MemoryManualClearanceOverrideStore(
        mutableMapOf<String, ManualClearanceOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualClearanceOverrideStore(
    var inner: ManualClearanceOverrideStore,
    var cache: TtlCache<ManualClearanceOverride>,
    var hits: Int,
    var misses: Int,
) : ManualClearanceOverrideStore {
    override fun find(code: String): ManualClearanceOverride? {
        val hit = (cache.get(code) as ManualClearanceOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualClearanceOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualClearanceOverride> = inner.list()
}

fun newCachedManualClearanceOverrideStore(inner: ManualClearanceOverrideStore, ttl: Int, clock: Clock): ManualClearanceOverrideStore {
    return CachedManualClearanceOverrideStore(
        inner = inner,
        cache = newTtlCache<ManualClearanceOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual clearance overrides. Empty fields match all. */
data class ManualClearanceOverrideQuery(
    var source: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualClearanceOverride): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualClearanceOverride>): MutableList<ManualClearanceOverride> {
        val matched = filter(items) { candidate: ManualClearanceOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualClearanceOverrides(store: ManualClearanceOverrideStore, query: ManualClearanceOverrideQuery): MutableList<ManualClearanceOverride> {
    return query.apply(store.list())
}
