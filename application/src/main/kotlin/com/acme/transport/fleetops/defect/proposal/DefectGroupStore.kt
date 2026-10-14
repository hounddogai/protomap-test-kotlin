package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect groups by code. */
interface DefectGroupStore {
    fun find(code: String): DefectGroup?
    fun save(item: DefectGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectGroupStore(
    var items: MutableMap<String, DefectGroup>,
    var order: MutableList<String>,
) : DefectGroupStore {
    override fun find(code: String): DefectGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectGroup) {
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

    override fun list(): MutableList<DefectGroup> {
        val out = mutableListOf<DefectGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectGroupStore(): DefectGroupStore {
    return MemoryDefectGroupStore(mutableMapOf<String, DefectGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectGroupStore(
    var inner: DefectGroupStore,
    var cache: TtlCache<DefectGroup>,
    var hits: Int,
    var misses: Int,
) : DefectGroupStore {
    override fun find(code: String): DefectGroup? {
        val hit = (cache.get(code) as DefectGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectGroup> = inner.list()
}

fun newCachedDefectGroupStore(inner: DefectGroupStore, ttl: Int, clock: Clock): DefectGroupStore {
    return CachedDefectGroupStore(
        inner = inner,
        cache = newTtlCache<DefectGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect groups. Empty fields match all. */
data class DefectGroupQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectGroup): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectGroup>): MutableList<DefectGroup> {
        val matched = filter(items) { candidate: DefectGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectGroups(store: DefectGroupStore, query: DefectGroupQuery): MutableList<DefectGroup> {
    return query.apply(store.list())
}
