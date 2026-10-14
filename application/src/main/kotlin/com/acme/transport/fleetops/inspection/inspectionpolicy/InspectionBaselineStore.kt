package com.acme.transport.fleetops.inspection.inspectionpolicy

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection baselines by code. */
interface InspectionBaselineStore {
    fun find(code: String): InspectionBaseline?
    fun save(item: InspectionBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionBaselineStore(
    var items: MutableMap<String, InspectionBaseline>,
    var order: MutableList<String>,
) : InspectionBaselineStore {
    override fun find(code: String): InspectionBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionBaseline) {
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

    override fun list(): MutableList<InspectionBaseline> {
        val out = mutableListOf<InspectionBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionBaselineStore(): InspectionBaselineStore {
    return MemoryInspectionBaselineStore(mutableMapOf<String, InspectionBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionBaselineStore(
    var inner: InspectionBaselineStore,
    var cache: TtlCache<InspectionBaseline>,
    var hits: Int,
    var misses: Int,
) : InspectionBaselineStore {
    override fun find(code: String): InspectionBaseline? {
        val hit = (cache.get(code) as InspectionBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionBaseline> = inner.list()
}

fun newCachedInspectionBaselineStore(inner: InspectionBaselineStore, ttl: Int, clock: Clock): InspectionBaselineStore {
    return CachedInspectionBaselineStore(
        inner = inner,
        cache = newTtlCache<InspectionBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection baselines. Empty fields match all. */
data class InspectionBaselineQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionBaseline>): MutableList<InspectionBaseline> {
        val matched = filter(items) { candidate: InspectionBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionBaselines(store: InspectionBaselineStore, query: InspectionBaselineQuery): MutableList<InspectionBaseline> {
    return query.apply(store.list())
}
