package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional relay policies by code. */
interface RegionalRelayPolicyStore {
    fun find(code: String): RegionalRelayPolicy?
    fun save(item: RegionalRelayPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRelayPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRelayPolicyStore(
    var items: MutableMap<String, RegionalRelayPolicy>,
    var order: MutableList<String>,
) : RegionalRelayPolicyStore {
    override fun find(code: String): RegionalRelayPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRelayPolicy) {
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

    override fun list(): MutableList<RegionalRelayPolicy> {
        val out = mutableListOf<RegionalRelayPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRelayPolicyStore(): RegionalRelayPolicyStore {
    return MemoryRegionalRelayPolicyStore(mutableMapOf<String, RegionalRelayPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRelayPolicyStore(
    var inner: RegionalRelayPolicyStore,
    var cache: TtlCache<RegionalRelayPolicy>,
    var hits: Int,
    var misses: Int,
) : RegionalRelayPolicyStore {
    override fun find(code: String): RegionalRelayPolicy? {
        val hit = (cache.get(code) as RegionalRelayPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRelayPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRelayPolicy> = inner.list()
}

fun newCachedRegionalRelayPolicyStore(inner: RegionalRelayPolicyStore, ttl: Int, clock: Clock): RegionalRelayPolicyStore {
    return CachedRegionalRelayPolicyStore(
        inner = inner,
        cache = newTtlCache<RegionalRelayPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional relay policies. Empty fields match all. */
data class RegionalRelayPolicyQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRelayPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalRelayPolicy>): MutableList<RegionalRelayPolicy> {
        val matched = filter(items) { candidate: RegionalRelayPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRelayPolicies(store: RegionalRelayPolicyStore, query: RegionalRelayPolicyQuery): MutableList<RegionalRelayPolicy> {
    return query.apply(store.list())
}
