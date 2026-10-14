package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard assignments by code. */
interface ScorecardAssignmentStore {
    fun find(code: String): ScorecardAssignment?
    fun save(item: ScorecardAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardAssignmentStore(
    var items: MutableMap<String, ScorecardAssignment>,
    var order: MutableList<String>,
) : ScorecardAssignmentStore {
    override fun find(code: String): ScorecardAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardAssignment) {
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

    override fun list(): MutableList<ScorecardAssignment> {
        val out = mutableListOf<ScorecardAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardAssignmentStore(): ScorecardAssignmentStore {
    return MemoryScorecardAssignmentStore(mutableMapOf<String, ScorecardAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardAssignmentStore(
    var inner: ScorecardAssignmentStore,
    var cache: TtlCache<ScorecardAssignment>,
    var hits: Int,
    var misses: Int,
) : ScorecardAssignmentStore {
    override fun find(code: String): ScorecardAssignment? {
        val hit = (cache.get(code) as ScorecardAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardAssignment> = inner.list()
}

fun newCachedScorecardAssignmentStore(inner: ScorecardAssignmentStore, ttl: Int, clock: Clock): ScorecardAssignmentStore {
    return CachedScorecardAssignmentStore(
        inner = inner,
        cache = newTtlCache<ScorecardAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard assignments. Empty fields match all. */
data class ScorecardAssignmentQuery(
    var channel: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardAssignment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<ScorecardAssignment>): MutableList<ScorecardAssignment> {
        val matched = filter(items) { candidate: ScorecardAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardAssignments(store: ScorecardAssignmentStore, query: ScorecardAssignmentQuery): MutableList<ScorecardAssignment> {
    return query.apply(store.list())
}
