package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict near miss policies by code. */
interface StrictNearMissPolicyStore {
    fun find(code: String): StrictNearMissPolicy?
    fun save(item: StrictNearMissPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictNearMissPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictNearMissPolicyStore(
    var items: MutableMap<String, StrictNearMissPolicy>,
    var order: MutableList<String>,
) : StrictNearMissPolicyStore {
    override fun find(code: String): StrictNearMissPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictNearMissPolicy) {
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

    override fun list(): MutableList<StrictNearMissPolicy> {
        val out = mutableListOf<StrictNearMissPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictNearMissPolicyStore(): StrictNearMissPolicyStore {
    return MemoryStrictNearMissPolicyStore(
        mutableMapOf<String, StrictNearMissPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictNearMissPolicyStore(
    var inner: StrictNearMissPolicyStore,
    var cache: TtlCache<StrictNearMissPolicy>,
    var hits: Int,
    var misses: Int,
) : StrictNearMissPolicyStore {
    override fun find(code: String): StrictNearMissPolicy? {
        val hit = (cache.get(code) as StrictNearMissPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictNearMissPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictNearMissPolicy> = inner.list()
}

fun newCachedStrictNearMissPolicyStore(inner: StrictNearMissPolicyStore, ttl: Int, clock: Clock): StrictNearMissPolicyStore {
    return CachedStrictNearMissPolicyStore(
        inner = inner,
        cache = newTtlCache<StrictNearMissPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict near miss policies. Empty fields match all. */
data class StrictNearMissPolicyQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictNearMissPolicy): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictNearMissPolicy>): MutableList<StrictNearMissPolicy> {
        val matched = filter(items) { candidate: StrictNearMissPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictNearMissPolicies(store: StrictNearMissPolicyStore, query: StrictNearMissPolicyQuery): MutableList<StrictNearMissPolicy> {
    return query.apply(store.list())
}
