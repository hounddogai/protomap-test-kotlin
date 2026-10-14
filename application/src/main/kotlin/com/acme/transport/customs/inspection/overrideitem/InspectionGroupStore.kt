package com.acme.transport.customs.inspection.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection groups by code. */
interface InspectionGroupStore {
    fun find(code: String): InspectionGroup?
    fun save(item: InspectionGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionGroupStore(
    var items: MutableMap<String, InspectionGroup>,
    var order: MutableList<String>,
) : InspectionGroupStore {
    override fun find(code: String): InspectionGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionGroup) {
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

    override fun list(): MutableList<InspectionGroup> {
        val out = mutableListOf<InspectionGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionGroupStore(): InspectionGroupStore {
    return MemoryInspectionGroupStore(mutableMapOf<String, InspectionGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionGroupStore(
    var inner: InspectionGroupStore,
    var cache: TtlCache<InspectionGroup>,
    var hits: Int,
    var misses: Int,
) : InspectionGroupStore {
    override fun find(code: String): InspectionGroup? {
        val hit = (cache.get(code) as InspectionGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionGroup> = inner.list()
}

fun newCachedInspectionGroupStore(inner: InspectionGroupStore, ttl: Int, clock: Clock): InspectionGroupStore {
    return CachedInspectionGroupStore(
        inner = inner,
        cache = newTtlCache<InspectionGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection groups. Empty fields match all. */
data class InspectionGroupQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionGroup>): MutableList<InspectionGroup> {
        val matched = filter(items) { candidate: InspectionGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionGroups(store: InspectionGroupStore, query: InspectionGroupQuery): MutableList<InspectionGroup> {
    return query.apply(store.list())
}
