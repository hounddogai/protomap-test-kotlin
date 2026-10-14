package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident cycles by code. */
interface IncidentCycleStore {
    fun find(code: String): IncidentCycle?
    fun save(item: IncidentCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentCycleStore(
    var items: MutableMap<String, IncidentCycle>,
    var order: MutableList<String>,
) : IncidentCycleStore {
    override fun find(code: String): IncidentCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentCycle) {
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

    override fun list(): MutableList<IncidentCycle> {
        val out = mutableListOf<IncidentCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentCycleStore(): IncidentCycleStore {
    return MemoryIncidentCycleStore(mutableMapOf<String, IncidentCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentCycleStore(
    var inner: IncidentCycleStore,
    var cache: TtlCache<IncidentCycle>,
    var hits: Int,
    var misses: Int,
) : IncidentCycleStore {
    override fun find(code: String): IncidentCycle? {
        val hit = (cache.get(code) as IncidentCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentCycle> = inner.list()
}

fun newCachedIncidentCycleStore(inner: IncidentCycleStore, ttl: Int, clock: Clock): IncidentCycleStore {
    return CachedIncidentCycleStore(
        inner = inner,
        cache = newTtlCache<IncidentCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident cycles. Empty fields match all. */
data class IncidentCycleQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentCycle>): MutableList<IncidentCycle> {
        val matched = filter(items) { candidate: IncidentCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentCycles(store: IncidentCycleStore, query: IncidentCycleQuery): MutableList<IncidentCycle> {
    return query.apply(store.list())
}
