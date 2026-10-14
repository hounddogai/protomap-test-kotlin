package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay milestones by code. */
interface RelayMilestoneStore {
    fun find(code: String): RelayMilestone?
    fun save(item: RelayMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayMilestoneStore(
    var items: MutableMap<String, RelayMilestone>,
    var order: MutableList<String>,
) : RelayMilestoneStore {
    override fun find(code: String): RelayMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayMilestone) {
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

    override fun list(): MutableList<RelayMilestone> {
        val out = mutableListOf<RelayMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayMilestoneStore(): RelayMilestoneStore {
    return MemoryRelayMilestoneStore(mutableMapOf<String, RelayMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayMilestoneStore(
    var inner: RelayMilestoneStore,
    var cache: TtlCache<RelayMilestone>,
    var hits: Int,
    var misses: Int,
) : RelayMilestoneStore {
    override fun find(code: String): RelayMilestone? {
        val hit = (cache.get(code) as RelayMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayMilestone> = inner.list()
}

fun newCachedRelayMilestoneStore(inner: RelayMilestoneStore, ttl: Int, clock: Clock): RelayMilestoneStore {
    return CachedRelayMilestoneStore(
        inner = inner,
        cache = newTtlCache<RelayMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay milestones. Empty fields match all. */
data class RelayMilestoneQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayMilestone>): MutableList<RelayMilestone> {
        val matched = filter(items) { candidate: RelayMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayMilestones(store: RelayMilestoneStore, query: RelayMilestoneQuery): MutableList<RelayMilestone> {
    return query.apply(store.list())
}
