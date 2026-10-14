package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss policies by code. */
interface NearMissPolicyStore {
    fun find(code: String): NearMissPolicy?
    fun save(item: NearMissPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissPolicyStore(
    var items: MutableMap<String, NearMissPolicy>,
    var order: MutableList<String>,
) : NearMissPolicyStore {
    override fun find(code: String): NearMissPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissPolicy) {
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

    override fun list(): MutableList<NearMissPolicy> {
        val out = mutableListOf<NearMissPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissPolicyStore(): NearMissPolicyStore {
    return MemoryNearMissPolicyStore(mutableMapOf<String, NearMissPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissPolicyStore(
    var inner: NearMissPolicyStore,
    var cache: TtlCache<NearMissPolicy>,
    var hits: Int,
    var misses: Int,
) : NearMissPolicyStore {
    override fun find(code: String): NearMissPolicy? {
        val hit = (cache.get(code) as NearMissPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissPolicy> = inner.list()
}

fun newCachedNearMissPolicyStore(inner: NearMissPolicyStore, ttl: Int, clock: Clock): NearMissPolicyStore {
    return CachedNearMissPolicyStore(
        inner = inner,
        cache = newTtlCache<NearMissPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss policies. Empty fields match all. */
data class NearMissPolicyQuery(
    var unit: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissPolicy): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<NearMissPolicy>): MutableList<NearMissPolicy> {
        val matched = filter(items) { candidate: NearMissPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissPolicies(store: NearMissPolicyStore, query: NearMissPolicyQuery): MutableList<NearMissPolicy> {
    return query.apply(store.list())
}
