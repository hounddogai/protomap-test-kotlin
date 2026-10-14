package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial milestones by code. */
interface AccessorialMilestoneStore {
    fun find(code: String): AccessorialMilestone?
    fun save(item: AccessorialMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialMilestoneStore(
    var items: MutableMap<String, AccessorialMilestone>,
    var order: MutableList<String>,
) : AccessorialMilestoneStore {
    override fun find(code: String): AccessorialMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialMilestone) {
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

    override fun list(): MutableList<AccessorialMilestone> {
        val out = mutableListOf<AccessorialMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialMilestoneStore(): AccessorialMilestoneStore {
    return MemoryAccessorialMilestoneStore(
        mutableMapOf<String, AccessorialMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialMilestoneStore(
    var inner: AccessorialMilestoneStore,
    var cache: TtlCache<AccessorialMilestone>,
    var hits: Int,
    var misses: Int,
) : AccessorialMilestoneStore {
    override fun find(code: String): AccessorialMilestone? {
        val hit = (cache.get(code) as AccessorialMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialMilestone> = inner.list()
}

fun newCachedAccessorialMilestoneStore(inner: AccessorialMilestoneStore, ttl: Int, clock: Clock): AccessorialMilestoneStore {
    return CachedAccessorialMilestoneStore(
        inner = inner,
        cache = newTtlCache<AccessorialMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial milestones. Empty fields match all. */
data class AccessorialMilestoneQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialMilestone): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialMilestone>): MutableList<AccessorialMilestone> {
        val matched = filter(items) { candidate: AccessorialMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialMilestones(store: AccessorialMilestoneStore, query: AccessorialMilestoneQuery): MutableList<AccessorialMilestone> {
    return query.apply(store.list())
}
