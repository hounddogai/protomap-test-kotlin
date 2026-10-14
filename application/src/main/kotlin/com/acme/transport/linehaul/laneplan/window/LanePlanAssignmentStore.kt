package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan assignments by code. */
interface LanePlanAssignmentStore {
    fun find(code: String): LanePlanAssignment?
    fun save(item: LanePlanAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanAssignmentStore(
    var items: MutableMap<String, LanePlanAssignment>,
    var order: MutableList<String>,
) : LanePlanAssignmentStore {
    override fun find(code: String): LanePlanAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanAssignment) {
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

    override fun list(): MutableList<LanePlanAssignment> {
        val out = mutableListOf<LanePlanAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanAssignmentStore(): LanePlanAssignmentStore {
    return MemoryLanePlanAssignmentStore(mutableMapOf<String, LanePlanAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanAssignmentStore(
    var inner: LanePlanAssignmentStore,
    var cache: TtlCache<LanePlanAssignment>,
    var hits: Int,
    var misses: Int,
) : LanePlanAssignmentStore {
    override fun find(code: String): LanePlanAssignment? {
        val hit = (cache.get(code) as LanePlanAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanAssignment> = inner.list()
}

fun newCachedLanePlanAssignmentStore(inner: LanePlanAssignmentStore, ttl: Int, clock: Clock): LanePlanAssignmentStore {
    return CachedLanePlanAssignmentStore(
        inner = inner,
        cache = newTtlCache<LanePlanAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan assignments. Empty fields match all. */
data class LanePlanAssignmentQuery(
    var source: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanAssignment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanAssignment>): MutableList<LanePlanAssignment> {
        val matched = filter(items) { candidate: LanePlanAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanAssignments(store: LanePlanAssignmentStore, query: LanePlanAssignmentQuery): MutableList<LanePlanAssignment> {
    return query.apply(store.list())
}
