package com.acme.transport.safety.incident.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default incident cycles by code. */
interface DefaultIncidentCycleStore {
    fun find(code: String): DefaultIncidentCycle?
    fun save(item: DefaultIncidentCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultIncidentCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultIncidentCycleStore(
    var items: MutableMap<String, DefaultIncidentCycle>,
    var order: MutableList<String>,
) : DefaultIncidentCycleStore {
    override fun find(code: String): DefaultIncidentCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultIncidentCycle) {
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

    override fun list(): MutableList<DefaultIncidentCycle> {
        val out = mutableListOf<DefaultIncidentCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultIncidentCycleStore(): DefaultIncidentCycleStore {
    return MemoryDefaultIncidentCycleStore(
        mutableMapOf<String, DefaultIncidentCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultIncidentCycleStore(
    var inner: DefaultIncidentCycleStore,
    var cache: TtlCache<DefaultIncidentCycle>,
    var hits: Int,
    var misses: Int,
) : DefaultIncidentCycleStore {
    override fun find(code: String): DefaultIncidentCycle? {
        val hit = (cache.get(code) as DefaultIncidentCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultIncidentCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultIncidentCycle> = inner.list()
}

fun newCachedDefaultIncidentCycleStore(inner: DefaultIncidentCycleStore, ttl: Int, clock: Clock): DefaultIncidentCycleStore {
    return CachedDefaultIncidentCycleStore(
        inner = inner,
        cache = newTtlCache<DefaultIncidentCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default incident cycles. Empty fields match all. */
data class DefaultIncidentCycleQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultIncidentCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<DefaultIncidentCycle>): MutableList<DefaultIncidentCycle> {
        val matched = filter(items) { candidate: DefaultIncidentCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultIncidentCycles(store: DefaultIncidentCycleStore, query: DefaultIncidentCycleQuery): MutableList<DefaultIncidentCycle> {
    return query.apply(store.list())
}
