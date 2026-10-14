package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker policies by code. */
interface LockerPolicyStore {
    fun find(code: String): LockerPolicy?
    fun save(item: LockerPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerPolicyStore(
    var items: MutableMap<String, LockerPolicy>,
    var order: MutableList<String>,
) : LockerPolicyStore {
    override fun find(code: String): LockerPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerPolicy) {
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

    override fun list(): MutableList<LockerPolicy> {
        val out = mutableListOf<LockerPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerPolicyStore(): LockerPolicyStore {
    return MemoryLockerPolicyStore(mutableMapOf<String, LockerPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerPolicyStore(
    var inner: LockerPolicyStore,
    var cache: TtlCache<LockerPolicy>,
    var hits: Int,
    var misses: Int,
) : LockerPolicyStore {
    override fun find(code: String): LockerPolicy? {
        val hit = (cache.get(code) as LockerPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerPolicy> = inner.list()
}

fun newCachedLockerPolicyStore(inner: LockerPolicyStore, ttl: Int, clock: Clock): LockerPolicyStore {
    return CachedLockerPolicyStore(
        inner = inner,
        cache = newTtlCache<LockerPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker policies. Empty fields match all. */
data class LockerPolicyQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerPolicy>): MutableList<LockerPolicy> {
        val matched = filter(items) { candidate: LockerPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerPolicies(store: LockerPolicyStore, query: LockerPolicyQuery): MutableList<LockerPolicy> {
    return query.apply(store.list())
}
