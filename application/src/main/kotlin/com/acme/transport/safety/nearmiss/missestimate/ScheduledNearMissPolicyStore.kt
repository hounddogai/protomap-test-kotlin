package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled near miss policies by code. */
interface ScheduledNearMissPolicyStore {
    fun find(code: String): ScheduledNearMissPolicy?
    fun save(item: ScheduledNearMissPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledNearMissPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledNearMissPolicyStore(
    var items: MutableMap<String, ScheduledNearMissPolicy>,
    var order: MutableList<String>,
) : ScheduledNearMissPolicyStore {
    override fun find(code: String): ScheduledNearMissPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledNearMissPolicy) {
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

    override fun list(): MutableList<ScheduledNearMissPolicy> {
        val out = mutableListOf<ScheduledNearMissPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledNearMissPolicyStore(): ScheduledNearMissPolicyStore {
    return MemoryScheduledNearMissPolicyStore(
        mutableMapOf<String, ScheduledNearMissPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledNearMissPolicyStore(
    var inner: ScheduledNearMissPolicyStore,
    var cache: TtlCache<ScheduledNearMissPolicy>,
    var hits: Int,
    var misses: Int,
) : ScheduledNearMissPolicyStore {
    override fun find(code: String): ScheduledNearMissPolicy? {
        val hit = (cache.get(code) as ScheduledNearMissPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledNearMissPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledNearMissPolicy> = inner.list()
}

fun newCachedScheduledNearMissPolicyStore(inner: ScheduledNearMissPolicyStore, ttl: Int, clock: Clock): ScheduledNearMissPolicyStore {
    return CachedScheduledNearMissPolicyStore(
        inner = inner,
        cache = newTtlCache<ScheduledNearMissPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled near miss policies. Empty fields match all. */
data class ScheduledNearMissPolicyQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledNearMissPolicy): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ScheduledNearMissPolicy>): MutableList<ScheduledNearMissPolicy> {
        val matched = filter(items) { candidate: ScheduledNearMissPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledNearMissPolicies(store: ScheduledNearMissPolicyStore, query: ScheduledNearMissPolicyQuery): MutableList<ScheduledNearMissPolicy> {
    return query.apply(store.list())
}
