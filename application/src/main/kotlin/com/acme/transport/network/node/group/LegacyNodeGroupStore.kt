package com.acme.transport.network.node.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy node groups by code. */
interface LegacyNodeGroupStore {
    fun find(code: String): LegacyNodeGroup?
    fun save(item: LegacyNodeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyNodeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyNodeGroupStore(
    var items: MutableMap<String, LegacyNodeGroup>,
    var order: MutableList<String>,
) : LegacyNodeGroupStore {
    override fun find(code: String): LegacyNodeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyNodeGroup) {
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

    override fun list(): MutableList<LegacyNodeGroup> {
        val out = mutableListOf<LegacyNodeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyNodeGroupStore(): LegacyNodeGroupStore {
    return MemoryLegacyNodeGroupStore(mutableMapOf<String, LegacyNodeGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyNodeGroupStore(
    var inner: LegacyNodeGroupStore,
    var cache: TtlCache<LegacyNodeGroup>,
    var hits: Int,
    var misses: Int,
) : LegacyNodeGroupStore {
    override fun find(code: String): LegacyNodeGroup? {
        val hit = (cache.get(code) as LegacyNodeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyNodeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyNodeGroup> = inner.list()
}

fun newCachedLegacyNodeGroupStore(inner: LegacyNodeGroupStore, ttl: Int, clock: Clock): LegacyNodeGroupStore {
    return CachedLegacyNodeGroupStore(
        inner = inner,
        cache = newTtlCache<LegacyNodeGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy node groups. Empty fields match all. */
data class LegacyNodeGroupQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyNodeGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyNodeGroup>): MutableList<LegacyNodeGroup> {
        val matched = filter(items) { candidate: LegacyNodeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyNodeGroups(store: LegacyNodeGroupStore, query: LegacyNodeGroupQuery): MutableList<LegacyNodeGroup> {
    return query.apply(store.list())
}
