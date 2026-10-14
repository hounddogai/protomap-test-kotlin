package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled backhaul policies by code. */
interface ScheduledBackhaulPolicyStore {
    fun find(code: String): ScheduledBackhaulPolicy?
    fun save(item: ScheduledBackhaulPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledBackhaulPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledBackhaulPolicyStore(
    var items: MutableMap<String, ScheduledBackhaulPolicy>,
    var order: MutableList<String>,
) : ScheduledBackhaulPolicyStore {
    override fun find(code: String): ScheduledBackhaulPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledBackhaulPolicy) {
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

    override fun list(): MutableList<ScheduledBackhaulPolicy> {
        val out = mutableListOf<ScheduledBackhaulPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledBackhaulPolicyStore(): ScheduledBackhaulPolicyStore {
    return MemoryScheduledBackhaulPolicyStore(
        mutableMapOf<String, ScheduledBackhaulPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledBackhaulPolicyStore(
    var inner: ScheduledBackhaulPolicyStore,
    var cache: TtlCache<ScheduledBackhaulPolicy>,
    var hits: Int,
    var misses: Int,
) : ScheduledBackhaulPolicyStore {
    override fun find(code: String): ScheduledBackhaulPolicy? {
        val hit = (cache.get(code) as ScheduledBackhaulPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledBackhaulPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledBackhaulPolicy> = inner.list()
}

fun newCachedScheduledBackhaulPolicyStore(inner: ScheduledBackhaulPolicyStore, ttl: Int, clock: Clock): ScheduledBackhaulPolicyStore {
    return CachedScheduledBackhaulPolicyStore(
        inner = inner,
        cache = newTtlCache<ScheduledBackhaulPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled backhaul policies. Empty fields match all. */
data class ScheduledBackhaulPolicyQuery(
    var lane: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledBackhaulPolicy): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<ScheduledBackhaulPolicy>): MutableList<ScheduledBackhaulPolicy> {
        val matched = filter(items) { candidate: ScheduledBackhaulPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledBackhaulPolicies(store: ScheduledBackhaulPolicyStore, query: ScheduledBackhaulPolicyQuery): MutableList<ScheduledBackhaulPolicy> {
    return query.apply(store.list())
}
