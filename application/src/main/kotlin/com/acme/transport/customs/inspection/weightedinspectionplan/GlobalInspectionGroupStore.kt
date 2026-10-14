package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global inspection groups by code. */
interface GlobalInspectionGroupStore {
    fun find(code: String): GlobalInspectionGroup?
    fun save(item: GlobalInspectionGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalInspectionGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalInspectionGroupStore(
    var items: MutableMap<String, GlobalInspectionGroup>,
    var order: MutableList<String>,
) : GlobalInspectionGroupStore {
    override fun find(code: String): GlobalInspectionGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalInspectionGroup) {
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

    override fun list(): MutableList<GlobalInspectionGroup> {
        val out = mutableListOf<GlobalInspectionGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalInspectionGroupStore(): GlobalInspectionGroupStore {
    return MemoryGlobalInspectionGroupStore(
        mutableMapOf<String, GlobalInspectionGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalInspectionGroupStore(
    var inner: GlobalInspectionGroupStore,
    var cache: TtlCache<GlobalInspectionGroup>,
    var hits: Int,
    var misses: Int,
) : GlobalInspectionGroupStore {
    override fun find(code: String): GlobalInspectionGroup? {
        val hit = (cache.get(code) as GlobalInspectionGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalInspectionGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalInspectionGroup> = inner.list()
}

fun newCachedGlobalInspectionGroupStore(inner: GlobalInspectionGroupStore, ttl: Int, clock: Clock): GlobalInspectionGroupStore {
    return CachedGlobalInspectionGroupStore(
        inner = inner,
        cache = newTtlCache<GlobalInspectionGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global inspection groups. Empty fields match all. */
data class GlobalInspectionGroupQuery(
    var locale: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalInspectionGroup): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<GlobalInspectionGroup>): MutableList<GlobalInspectionGroup> {
        val matched = filter(items) { candidate: GlobalInspectionGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalInspectionGroups(store: GlobalInspectionGroupStore, query: GlobalInspectionGroupQuery): MutableList<GlobalInspectionGroup> {
    return query.apply(store.list())
}
