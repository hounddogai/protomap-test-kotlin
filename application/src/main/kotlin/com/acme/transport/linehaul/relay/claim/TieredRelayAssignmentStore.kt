package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered relay assignments by code. */
interface TieredRelayAssignmentStore {
    fun find(code: String): TieredRelayAssignment?
    fun save(item: TieredRelayAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRelayAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRelayAssignmentStore(
    var items: MutableMap<String, TieredRelayAssignment>,
    var order: MutableList<String>,
) : TieredRelayAssignmentStore {
    override fun find(code: String): TieredRelayAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRelayAssignment) {
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

    override fun list(): MutableList<TieredRelayAssignment> {
        val out = mutableListOf<TieredRelayAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRelayAssignmentStore(): TieredRelayAssignmentStore {
    return MemoryTieredRelayAssignmentStore(
        mutableMapOf<String, TieredRelayAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRelayAssignmentStore(
    var inner: TieredRelayAssignmentStore,
    var cache: TtlCache<TieredRelayAssignment>,
    var hits: Int,
    var misses: Int,
) : TieredRelayAssignmentStore {
    override fun find(code: String): TieredRelayAssignment? {
        val hit = (cache.get(code) as TieredRelayAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRelayAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRelayAssignment> = inner.list()
}

fun newCachedTieredRelayAssignmentStore(inner: TieredRelayAssignmentStore, ttl: Int, clock: Clock): TieredRelayAssignmentStore {
    return CachedTieredRelayAssignmentStore(
        inner = inner,
        cache = newTtlCache<TieredRelayAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered relay assignments. Empty fields match all. */
data class TieredRelayAssignmentQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRelayAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRelayAssignment>): MutableList<TieredRelayAssignment> {
        val matched = filter(items) { candidate: TieredRelayAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRelayAssignments(store: TieredRelayAssignmentStore, query: TieredRelayAssignmentQuery): MutableList<TieredRelayAssignment> {
    return query.apply(store.list())
}
