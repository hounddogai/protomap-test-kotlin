package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global audit milestones by code. */
interface GlobalAuditMilestoneStore {
    fun find(code: String): GlobalAuditMilestone?
    fun save(item: GlobalAuditMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAuditMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAuditMilestoneStore(
    var items: MutableMap<String, GlobalAuditMilestone>,
    var order: MutableList<String>,
) : GlobalAuditMilestoneStore {
    override fun find(code: String): GlobalAuditMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAuditMilestone) {
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

    override fun list(): MutableList<GlobalAuditMilestone> {
        val out = mutableListOf<GlobalAuditMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAuditMilestoneStore(): GlobalAuditMilestoneStore {
    return MemoryGlobalAuditMilestoneStore(
        mutableMapOf<String, GlobalAuditMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAuditMilestoneStore(
    var inner: GlobalAuditMilestoneStore,
    var cache: TtlCache<GlobalAuditMilestone>,
    var hits: Int,
    var misses: Int,
) : GlobalAuditMilestoneStore {
    override fun find(code: String): GlobalAuditMilestone? {
        val hit = (cache.get(code) as GlobalAuditMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAuditMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAuditMilestone> = inner.list()
}

fun newCachedGlobalAuditMilestoneStore(inner: GlobalAuditMilestoneStore, ttl: Int, clock: Clock): GlobalAuditMilestoneStore {
    return CachedGlobalAuditMilestoneStore(
        inner = inner,
        cache = newTtlCache<GlobalAuditMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global audit milestones. Empty fields match all. */
data class GlobalAuditMilestoneQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAuditMilestone): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<GlobalAuditMilestone>): MutableList<GlobalAuditMilestone> {
        val matched = filter(items) { candidate: GlobalAuditMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAuditMilestones(store: GlobalAuditMilestoneStore, query: GlobalAuditMilestoneQuery): MutableList<GlobalAuditMilestone> {
    return query.apply(store.list())
}
