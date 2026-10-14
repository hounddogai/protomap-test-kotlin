package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global inspection templates by code. */
interface GlobalInspectionTemplateStore {
    fun find(code: String): GlobalInspectionTemplate?
    fun save(item: GlobalInspectionTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalInspectionTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalInspectionTemplateStore(
    var items: MutableMap<String, GlobalInspectionTemplate>,
    var order: MutableList<String>,
) : GlobalInspectionTemplateStore {
    override fun find(code: String): GlobalInspectionTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalInspectionTemplate) {
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

    override fun list(): MutableList<GlobalInspectionTemplate> {
        val out = mutableListOf<GlobalInspectionTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalInspectionTemplateStore(): GlobalInspectionTemplateStore {
    return MemoryGlobalInspectionTemplateStore(
        mutableMapOf<String, GlobalInspectionTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalInspectionTemplateStore(
    var inner: GlobalInspectionTemplateStore,
    var cache: TtlCache<GlobalInspectionTemplate>,
    var hits: Int,
    var misses: Int,
) : GlobalInspectionTemplateStore {
    override fun find(code: String): GlobalInspectionTemplate? {
        val hit = (cache.get(code) as GlobalInspectionTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalInspectionTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalInspectionTemplate> = inner.list()
}

fun newCachedGlobalInspectionTemplateStore(inner: GlobalInspectionTemplateStore, ttl: Int, clock: Clock): GlobalInspectionTemplateStore {
    return CachedGlobalInspectionTemplateStore(
        inner = inner,
        cache = newTtlCache<GlobalInspectionTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global inspection templates. Empty fields match all. */
data class GlobalInspectionTemplateQuery(
    var country: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalInspectionTemplate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<GlobalInspectionTemplate>): MutableList<GlobalInspectionTemplate> {
        val matched = filter(items) { candidate: GlobalInspectionTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalInspectionTemplates(store: GlobalInspectionTemplateStore, query: GlobalInspectionTemplateQuery): MutableList<GlobalInspectionTemplate> {
    return query.apply(store.list())
}
