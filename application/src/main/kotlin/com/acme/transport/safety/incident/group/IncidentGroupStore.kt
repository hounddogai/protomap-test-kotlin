package com.acme.transport.safety.incident.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident groups by code. */
interface IncidentGroupStore {
    fun find(code: String): IncidentGroup?
    fun save(item: IncidentGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentGroupStore(
    var items: MutableMap<String, IncidentGroup>,
    var order: MutableList<String>,
) : IncidentGroupStore {
    override fun find(code: String): IncidentGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentGroup) {
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

    override fun list(): MutableList<IncidentGroup> {
        val out = mutableListOf<IncidentGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentGroupStore(): IncidentGroupStore {
    return MemoryIncidentGroupStore(mutableMapOf<String, IncidentGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentGroupStore(
    var inner: IncidentGroupStore,
    var cache: TtlCache<IncidentGroup>,
    var hits: Int,
    var misses: Int,
) : IncidentGroupStore {
    override fun find(code: String): IncidentGroup? {
        val hit = (cache.get(code) as IncidentGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentGroup> = inner.list()
}

fun newCachedIncidentGroupStore(inner: IncidentGroupStore, ttl: Int, clock: Clock): IncidentGroupStore {
    return CachedIncidentGroupStore(
        inner = inner,
        cache = newTtlCache<IncidentGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident groups. Empty fields match all. */
data class IncidentGroupQuery(
    var lane: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentGroup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentGroup>): MutableList<IncidentGroup> {
        val matched = filter(items) { candidate: IncidentGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentGroups(store: IncidentGroupStore, query: IncidentGroupQuery): MutableList<IncidentGroup> {
    return query.apply(store.list())
}
