package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident rollups by code. */
interface IncidentRollupStore {
    fun find(code: String): IncidentRollup?
    fun save(item: IncidentRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentRollupStore(
    var items: MutableMap<String, IncidentRollup>,
    var order: MutableList<String>,
) : IncidentRollupStore {
    override fun find(code: String): IncidentRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentRollup) {
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

    override fun list(): MutableList<IncidentRollup> {
        val out = mutableListOf<IncidentRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentRollupStore(): IncidentRollupStore {
    return MemoryIncidentRollupStore(mutableMapOf<String, IncidentRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentRollupStore(
    var inner: IncidentRollupStore,
    var cache: TtlCache<IncidentRollup>,
    var hits: Int,
    var misses: Int,
) : IncidentRollupStore {
    override fun find(code: String): IncidentRollup? {
        val hit = (cache.get(code) as IncidentRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentRollup> = inner.list()
}

fun newCachedIncidentRollupStore(inner: IncidentRollupStore, ttl: Int, clock: Clock): IncidentRollupStore {
    return CachedIncidentRollupStore(
        inner = inner,
        cache = newTtlCache<IncidentRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident rollups. Empty fields match all. */
data class IncidentRollupQuery(
    var country: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentRollup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentRollup>): MutableList<IncidentRollup> {
        val matched = filter(items) { candidate: IncidentRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentRollups(store: IncidentRollupStore, query: IncidentRollupQuery): MutableList<IncidentRollup> {
    return query.apply(store.list())
}
