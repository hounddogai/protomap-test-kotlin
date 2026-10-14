package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling locker policies by code. */
interface RollingLockerPolicyStore {
    fun find(code: String): RollingLockerPolicy?
    fun save(item: RollingLockerPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingLockerPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingLockerPolicyStore(
    var items: MutableMap<String, RollingLockerPolicy>,
    var order: MutableList<String>,
) : RollingLockerPolicyStore {
    override fun find(code: String): RollingLockerPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingLockerPolicy) {
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

    override fun list(): MutableList<RollingLockerPolicy> {
        val out = mutableListOf<RollingLockerPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingLockerPolicyStore(): RollingLockerPolicyStore {
    return MemoryRollingLockerPolicyStore(mutableMapOf<String, RollingLockerPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingLockerPolicyStore(
    var inner: RollingLockerPolicyStore,
    var cache: TtlCache<RollingLockerPolicy>,
    var hits: Int,
    var misses: Int,
) : RollingLockerPolicyStore {
    override fun find(code: String): RollingLockerPolicy? {
        val hit = (cache.get(code) as RollingLockerPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingLockerPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingLockerPolicy> = inner.list()
}

fun newCachedRollingLockerPolicyStore(inner: RollingLockerPolicyStore, ttl: Int, clock: Clock): RollingLockerPolicyStore {
    return CachedRollingLockerPolicyStore(
        inner = inner,
        cache = newTtlCache<RollingLockerPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling locker policies. Empty fields match all. */
data class RollingLockerPolicyQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingLockerPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingLockerPolicy>): MutableList<RollingLockerPolicy> {
        val matched = filter(items) { candidate: RollingLockerPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingLockerPolicies(store: RollingLockerPolicyStore, query: RollingLockerPolicyQuery): MutableList<RollingLockerPolicy> {
    return query.apply(store.list())
}
