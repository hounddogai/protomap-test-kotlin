package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global parcel policies by code. */
interface GlobalParcelPolicyStore {
    fun find(code: String): GlobalParcelPolicy?
    fun save(item: GlobalParcelPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalParcelPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalParcelPolicyStore(
    var items: MutableMap<String, GlobalParcelPolicy>,
    var order: MutableList<String>,
) : GlobalParcelPolicyStore {
    override fun find(code: String): GlobalParcelPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalParcelPolicy) {
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

    override fun list(): MutableList<GlobalParcelPolicy> {
        val out = mutableListOf<GlobalParcelPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalParcelPolicyStore(): GlobalParcelPolicyStore {
    return MemoryGlobalParcelPolicyStore(mutableMapOf<String, GlobalParcelPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalParcelPolicyStore(
    var inner: GlobalParcelPolicyStore,
    var cache: TtlCache<GlobalParcelPolicy>,
    var hits: Int,
    var misses: Int,
) : GlobalParcelPolicyStore {
    override fun find(code: String): GlobalParcelPolicy? {
        val hit = (cache.get(code) as GlobalParcelPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalParcelPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalParcelPolicy> = inner.list()
}

fun newCachedGlobalParcelPolicyStore(inner: GlobalParcelPolicyStore, ttl: Int, clock: Clock): GlobalParcelPolicyStore {
    return CachedGlobalParcelPolicyStore(
        inner = inner,
        cache = newTtlCache<GlobalParcelPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global parcel policies. Empty fields match all. */
data class GlobalParcelPolicyQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalParcelPolicy): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalParcelPolicy>): MutableList<GlobalParcelPolicy> {
        val matched = filter(items) { candidate: GlobalParcelPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalParcelPolicies(store: GlobalParcelPolicyStore, query: GlobalParcelPolicyQuery): MutableList<GlobalParcelPolicy> {
    return query.apply(store.list())
}
