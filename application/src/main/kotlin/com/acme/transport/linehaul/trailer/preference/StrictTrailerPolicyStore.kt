package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict trailer policies by code. */
interface StrictTrailerPolicyStore {
    fun find(code: String): StrictTrailerPolicy?
    fun save(item: StrictTrailerPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTrailerPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTrailerPolicyStore(
    var items: MutableMap<String, StrictTrailerPolicy>,
    var order: MutableList<String>,
) : StrictTrailerPolicyStore {
    override fun find(code: String): StrictTrailerPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTrailerPolicy) {
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

    override fun list(): MutableList<StrictTrailerPolicy> {
        val out = mutableListOf<StrictTrailerPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTrailerPolicyStore(): StrictTrailerPolicyStore {
    return MemoryStrictTrailerPolicyStore(mutableMapOf<String, StrictTrailerPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTrailerPolicyStore(
    var inner: StrictTrailerPolicyStore,
    var cache: TtlCache<StrictTrailerPolicy>,
    var hits: Int,
    var misses: Int,
) : StrictTrailerPolicyStore {
    override fun find(code: String): StrictTrailerPolicy? {
        val hit = (cache.get(code) as StrictTrailerPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTrailerPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTrailerPolicy> = inner.list()
}

fun newCachedStrictTrailerPolicyStore(inner: StrictTrailerPolicyStore, ttl: Int, clock: Clock): StrictTrailerPolicyStore {
    return CachedStrictTrailerPolicyStore(
        inner = inner,
        cache = newTtlCache<StrictTrailerPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict trailer policies. Empty fields match all. */
data class StrictTrailerPolicyQuery(
    var channel: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTrailerPolicy): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTrailerPolicy>): MutableList<StrictTrailerPolicy> {
        val matched = filter(items) { candidate: StrictTrailerPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTrailerPolicies(store: StrictTrailerPolicyStore, query: StrictTrailerPolicyQuery): MutableList<StrictTrailerPolicy> {
    return query.apply(store.list())
}
