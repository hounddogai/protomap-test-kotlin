package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual inspection groups by code. */
interface ManualInspectionGroupStore {
    fun find(code: String): ManualInspectionGroup?
    fun save(item: ManualInspectionGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualInspectionGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualInspectionGroupStore(
    var items: MutableMap<String, ManualInspectionGroup>,
    var order: MutableList<String>,
) : ManualInspectionGroupStore {
    override fun find(code: String): ManualInspectionGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualInspectionGroup) {
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

    override fun list(): MutableList<ManualInspectionGroup> {
        val out = mutableListOf<ManualInspectionGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualInspectionGroupStore(): ManualInspectionGroupStore {
    return MemoryManualInspectionGroupStore(
        mutableMapOf<String, ManualInspectionGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualInspectionGroupStore(
    var inner: ManualInspectionGroupStore,
    var cache: TtlCache<ManualInspectionGroup>,
    var hits: Int,
    var misses: Int,
) : ManualInspectionGroupStore {
    override fun find(code: String): ManualInspectionGroup? {
        val hit = (cache.get(code) as ManualInspectionGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualInspectionGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualInspectionGroup> = inner.list()
}

fun newCachedManualInspectionGroupStore(inner: ManualInspectionGroupStore, ttl: Int, clock: Clock): ManualInspectionGroupStore {
    return CachedManualInspectionGroupStore(
        inner = inner,
        cache = newTtlCache<ManualInspectionGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual inspection groups. Empty fields match all. */
data class ManualInspectionGroupQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualInspectionGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualInspectionGroup>): MutableList<ManualInspectionGroup> {
        val matched = filter(items) { candidate: ManualInspectionGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualInspectionGroups(store: ManualInspectionGroupStore, query: ManualInspectionGroupQuery): MutableList<ManualInspectionGroup> {
    return query.apply(store.list())
}
