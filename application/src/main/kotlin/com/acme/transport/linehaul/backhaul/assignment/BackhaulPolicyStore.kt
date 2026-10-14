package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul policies by code. */
interface BackhaulPolicyStore {
    fun find(code: String): BackhaulPolicy?
    fun save(item: BackhaulPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulPolicyStore(
    var items: MutableMap<String, BackhaulPolicy>,
    var order: MutableList<String>,
) : BackhaulPolicyStore {
    override fun find(code: String): BackhaulPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulPolicy) {
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

    override fun list(): MutableList<BackhaulPolicy> {
        val out = mutableListOf<BackhaulPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulPolicyStore(): BackhaulPolicyStore {
    return MemoryBackhaulPolicyStore(mutableMapOf<String, BackhaulPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulPolicyStore(
    var inner: BackhaulPolicyStore,
    var cache: TtlCache<BackhaulPolicy>,
    var hits: Int,
    var misses: Int,
) : BackhaulPolicyStore {
    override fun find(code: String): BackhaulPolicy? {
        val hit = (cache.get(code) as BackhaulPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulPolicy> = inner.list()
}

fun newCachedBackhaulPolicyStore(inner: BackhaulPolicyStore, ttl: Int, clock: Clock): BackhaulPolicyStore {
    return CachedBackhaulPolicyStore(
        inner = inner,
        cache = newTtlCache<BackhaulPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul policies. Empty fields match all. */
data class BackhaulPolicyQuery(
    var depot: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulPolicy): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulPolicy>): MutableList<BackhaulPolicy> {
        val matched = filter(items) { candidate: BackhaulPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulPolicies(store: BackhaulPolicyStore, query: BackhaulPolicyQuery): MutableList<BackhaulPolicy> {
    return query.apply(store.list())
}
