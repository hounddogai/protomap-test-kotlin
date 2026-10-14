package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual relay assignments by code. */
interface ManualRelayAssignmentStore {
    fun find(code: String): ManualRelayAssignment?
    fun save(item: ManualRelayAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRelayAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRelayAssignmentStore(
    var items: MutableMap<String, ManualRelayAssignment>,
    var order: MutableList<String>,
) : ManualRelayAssignmentStore {
    override fun find(code: String): ManualRelayAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRelayAssignment) {
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

    override fun list(): MutableList<ManualRelayAssignment> {
        val out = mutableListOf<ManualRelayAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRelayAssignmentStore(): ManualRelayAssignmentStore {
    return MemoryManualRelayAssignmentStore(
        mutableMapOf<String, ManualRelayAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRelayAssignmentStore(
    var inner: ManualRelayAssignmentStore,
    var cache: TtlCache<ManualRelayAssignment>,
    var hits: Int,
    var misses: Int,
) : ManualRelayAssignmentStore {
    override fun find(code: String): ManualRelayAssignment? {
        val hit = (cache.get(code) as ManualRelayAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRelayAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRelayAssignment> = inner.list()
}

fun newCachedManualRelayAssignmentStore(inner: ManualRelayAssignmentStore, ttl: Int, clock: Clock): ManualRelayAssignmentStore {
    return CachedManualRelayAssignmentStore(
        inner = inner,
        cache = newTtlCache<ManualRelayAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual relay assignments. Empty fields match all. */
data class ManualRelayAssignmentQuery(
    var region: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRelayAssignment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRelayAssignment>): MutableList<ManualRelayAssignment> {
        val matched = filter(items) { candidate: ManualRelayAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRelayAssignments(store: ManualRelayAssignmentStore, query: ManualRelayAssignmentQuery): MutableList<ManualRelayAssignment> {
    return query.apply(store.list())
}
