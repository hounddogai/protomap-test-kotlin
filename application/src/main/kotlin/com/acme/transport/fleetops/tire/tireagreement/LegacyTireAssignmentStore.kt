package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy tire assignments by code. */
interface LegacyTireAssignmentStore {
    fun find(code: String): LegacyTireAssignment?
    fun save(item: LegacyTireAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTireAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTireAssignmentStore(
    var items: MutableMap<String, LegacyTireAssignment>,
    var order: MutableList<String>,
) : LegacyTireAssignmentStore {
    override fun find(code: String): LegacyTireAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTireAssignment) {
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

    override fun list(): MutableList<LegacyTireAssignment> {
        val out = mutableListOf<LegacyTireAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTireAssignmentStore(): LegacyTireAssignmentStore {
    return MemoryLegacyTireAssignmentStore(
        mutableMapOf<String, LegacyTireAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTireAssignmentStore(
    var inner: LegacyTireAssignmentStore,
    var cache: TtlCache<LegacyTireAssignment>,
    var hits: Int,
    var misses: Int,
) : LegacyTireAssignmentStore {
    override fun find(code: String): LegacyTireAssignment? {
        val hit = (cache.get(code) as LegacyTireAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTireAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTireAssignment> = inner.list()
}

fun newCachedLegacyTireAssignmentStore(inner: LegacyTireAssignmentStore, ttl: Int, clock: Clock): LegacyTireAssignmentStore {
    return CachedLegacyTireAssignmentStore(
        inner = inner,
        cache = newTtlCache<LegacyTireAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy tire assignments. Empty fields match all. */
data class LegacyTireAssignmentQuery(
    var segment: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTireAssignment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyTireAssignment>): MutableList<LegacyTireAssignment> {
        val matched = filter(items) { candidate: LegacyTireAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTireAssignments(store: LegacyTireAssignmentStore, query: LegacyTireAssignmentQuery): MutableList<LegacyTireAssignment> {
    return query.apply(store.list())
}
