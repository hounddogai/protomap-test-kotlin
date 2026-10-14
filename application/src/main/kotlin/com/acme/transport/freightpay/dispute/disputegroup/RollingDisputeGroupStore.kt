package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling dispute groups by code. */
interface RollingDisputeGroupStore {
    fun find(code: String): RollingDisputeGroup?
    fun save(item: RollingDisputeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDisputeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDisputeGroupStore(
    var items: MutableMap<String, RollingDisputeGroup>,
    var order: MutableList<String>,
) : RollingDisputeGroupStore {
    override fun find(code: String): RollingDisputeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDisputeGroup) {
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

    override fun list(): MutableList<RollingDisputeGroup> {
        val out = mutableListOf<RollingDisputeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDisputeGroupStore(): RollingDisputeGroupStore {
    return MemoryRollingDisputeGroupStore(mutableMapOf<String, RollingDisputeGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDisputeGroupStore(
    var inner: RollingDisputeGroupStore,
    var cache: TtlCache<RollingDisputeGroup>,
    var hits: Int,
    var misses: Int,
) : RollingDisputeGroupStore {
    override fun find(code: String): RollingDisputeGroup? {
        val hit = (cache.get(code) as RollingDisputeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDisputeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDisputeGroup> = inner.list()
}

fun newCachedRollingDisputeGroupStore(inner: RollingDisputeGroupStore, ttl: Int, clock: Clock): RollingDisputeGroupStore {
    return CachedRollingDisputeGroupStore(
        inner = inner,
        cache = newTtlCache<RollingDisputeGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dispute groups. Empty fields match all. */
data class RollingDisputeGroupQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDisputeGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RollingDisputeGroup>): MutableList<RollingDisputeGroup> {
        val matched = filter(items) { candidate: RollingDisputeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDisputeGroups(store: RollingDisputeGroupStore, query: RollingDisputeGroupQuery): MutableList<RollingDisputeGroup> {
    return query.apply(store.list())
}
