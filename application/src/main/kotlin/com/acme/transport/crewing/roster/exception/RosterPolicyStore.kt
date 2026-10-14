package com.acme.transport.crewing.roster.exception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster policies by code. */
interface RosterPolicyStore {
    fun find(code: String): RosterPolicy?
    fun save(item: RosterPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterPolicyStore(
    var items: MutableMap<String, RosterPolicy>,
    var order: MutableList<String>,
) : RosterPolicyStore {
    override fun find(code: String): RosterPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterPolicy) {
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

    override fun list(): MutableList<RosterPolicy> {
        val out = mutableListOf<RosterPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterPolicyStore(): RosterPolicyStore {
    return MemoryRosterPolicyStore(mutableMapOf<String, RosterPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterPolicyStore(
    var inner: RosterPolicyStore,
    var cache: TtlCache<RosterPolicy>,
    var hits: Int,
    var misses: Int,
) : RosterPolicyStore {
    override fun find(code: String): RosterPolicy? {
        val hit = (cache.get(code) as RosterPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterPolicy> = inner.list()
}

fun newCachedRosterPolicyStore(inner: RosterPolicyStore, ttl: Int, clock: Clock): RosterPolicyStore {
    return CachedRosterPolicyStore(
        inner = inner,
        cache = newTtlCache<RosterPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster policies. Empty fields match all. */
data class RosterPolicyQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterPolicy): Boolean {
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

    fun apply(items: MutableList<RosterPolicy>): MutableList<RosterPolicy> {
        val matched = filter(items) { candidate: RosterPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterPolicies(store: RosterPolicyStore, query: RosterPolicyQuery): MutableList<RosterPolicy> {
    return query.apply(store.list())
}
