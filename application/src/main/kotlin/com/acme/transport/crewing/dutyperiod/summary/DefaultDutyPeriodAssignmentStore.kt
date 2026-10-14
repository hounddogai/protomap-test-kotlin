package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default duty period assignments by code. */
interface DefaultDutyPeriodAssignmentStore {
    fun find(code: String): DefaultDutyPeriodAssignment?
    fun save(item: DefaultDutyPeriodAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDutyPeriodAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDutyPeriodAssignmentStore(
    var items: MutableMap<String, DefaultDutyPeriodAssignment>,
    var order: MutableList<String>,
) : DefaultDutyPeriodAssignmentStore {
    override fun find(code: String): DefaultDutyPeriodAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDutyPeriodAssignment) {
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

    override fun list(): MutableList<DefaultDutyPeriodAssignment> {
        val out = mutableListOf<DefaultDutyPeriodAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDutyPeriodAssignmentStore(): DefaultDutyPeriodAssignmentStore {
    return MemoryDefaultDutyPeriodAssignmentStore(
        mutableMapOf<String, DefaultDutyPeriodAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDutyPeriodAssignmentStore(
    var inner: DefaultDutyPeriodAssignmentStore,
    var cache: TtlCache<DefaultDutyPeriodAssignment>,
    var hits: Int,
    var misses: Int,
) : DefaultDutyPeriodAssignmentStore {
    override fun find(code: String): DefaultDutyPeriodAssignment? {
        val hit = (cache.get(code) as DefaultDutyPeriodAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDutyPeriodAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDutyPeriodAssignment> = inner.list()
}

fun newCachedDefaultDutyPeriodAssignmentStore(inner: DefaultDutyPeriodAssignmentStore, ttl: Int, clock: Clock): DefaultDutyPeriodAssignmentStore {
    return CachedDefaultDutyPeriodAssignmentStore(
        inner = inner,
        cache = newTtlCache<DefaultDutyPeriodAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default duty period assignments. Empty fields match all. */
data class DefaultDutyPeriodAssignmentQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDutyPeriodAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DefaultDutyPeriodAssignment>): MutableList<DefaultDutyPeriodAssignment> {
        val matched = filter(items) { candidate: DefaultDutyPeriodAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDutyPeriodAssignments(store: DefaultDutyPeriodAssignmentStore, query: DefaultDutyPeriodAssignmentQuery): MutableList<DefaultDutyPeriodAssignment> {
    return query.apply(store.list())
}
