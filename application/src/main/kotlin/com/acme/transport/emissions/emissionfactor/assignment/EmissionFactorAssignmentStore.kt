package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor assignments by code. */
interface EmissionFactorAssignmentStore {
    fun find(code: String): EmissionFactorAssignment?
    fun save(item: EmissionFactorAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorAssignmentStore(
    var items: MutableMap<String, EmissionFactorAssignment>,
    var order: MutableList<String>,
) : EmissionFactorAssignmentStore {
    override fun find(code: String): EmissionFactorAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorAssignment) {
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

    override fun list(): MutableList<EmissionFactorAssignment> {
        val out = mutableListOf<EmissionFactorAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorAssignmentStore(): EmissionFactorAssignmentStore {
    return MemoryEmissionFactorAssignmentStore(
        mutableMapOf<String, EmissionFactorAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorAssignmentStore(
    var inner: EmissionFactorAssignmentStore,
    var cache: TtlCache<EmissionFactorAssignment>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorAssignmentStore {
    override fun find(code: String): EmissionFactorAssignment? {
        val hit = (cache.get(code) as EmissionFactorAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorAssignment> = inner.list()
}

fun newCachedEmissionFactorAssignmentStore(inner: EmissionFactorAssignmentStore, ttl: Int, clock: Clock): EmissionFactorAssignmentStore {
    return CachedEmissionFactorAssignmentStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor assignments. Empty fields match all. */
data class EmissionFactorAssignmentQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorAssignment>): MutableList<EmissionFactorAssignment> {
        val matched = filter(items) { candidate: EmissionFactorAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorAssignments(store: EmissionFactorAssignmentStore, query: EmissionFactorAssignmentQuery): MutableList<EmissionFactorAssignment> {
    return query.apply(store.list())
}
