package com.acme.transport.network.node.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node schedules by code. */
interface NodeScheduleStore {
    fun find(code: String): NodeSchedule?
    fun save(item: NodeSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeScheduleStore(
    var items: MutableMap<String, NodeSchedule>,
    var order: MutableList<String>,
) : NodeScheduleStore {
    override fun find(code: String): NodeSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeSchedule) {
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

    override fun list(): MutableList<NodeSchedule> {
        val out = mutableListOf<NodeSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeScheduleStore(): NodeScheduleStore {
    return MemoryNodeScheduleStore(mutableMapOf<String, NodeSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeScheduleStore(
    var inner: NodeScheduleStore,
    var cache: TtlCache<NodeSchedule>,
    var hits: Int,
    var misses: Int,
) : NodeScheduleStore {
    override fun find(code: String): NodeSchedule? {
        val hit = (cache.get(code) as NodeSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeSchedule> = inner.list()
}

fun newCachedNodeScheduleStore(inner: NodeScheduleStore, ttl: Int, clock: Clock): NodeScheduleStore {
    return CachedNodeScheduleStore(
        inner = inner,
        cache = newTtlCache<NodeSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node schedules. Empty fields match all. */
data class NodeScheduleQuery(
    var depot: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeSchedule): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeSchedule>): MutableList<NodeSchedule> {
        val matched = filter(items) { candidate: NodeSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeSchedules(store: NodeScheduleStore, query: NodeScheduleQuery): MutableList<NodeSchedule> {
    return query.apply(store.list())
}
