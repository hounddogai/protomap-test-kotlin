package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling lease return tiers by code. */
interface RollingLeaseReturnTierStore {
    fun find(code: String): RollingLeaseReturnTier?
    fun save(item: RollingLeaseReturnTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingLeaseReturnTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingLeaseReturnTierStore(
    var items: MutableMap<String, RollingLeaseReturnTier>,
    var order: MutableList<String>,
) : RollingLeaseReturnTierStore {
    override fun find(code: String): RollingLeaseReturnTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingLeaseReturnTier) {
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

    override fun list(): MutableList<RollingLeaseReturnTier> {
        val out = mutableListOf<RollingLeaseReturnTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingLeaseReturnTierStore(): RollingLeaseReturnTierStore {
    return MemoryRollingLeaseReturnTierStore(
        mutableMapOf<String, RollingLeaseReturnTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingLeaseReturnTierStore(
    var inner: RollingLeaseReturnTierStore,
    var cache: TtlCache<RollingLeaseReturnTier>,
    var hits: Int,
    var misses: Int,
) : RollingLeaseReturnTierStore {
    override fun find(code: String): RollingLeaseReturnTier? {
        val hit = (cache.get(code) as RollingLeaseReturnTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingLeaseReturnTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingLeaseReturnTier> = inner.list()
}

fun newCachedRollingLeaseReturnTierStore(inner: RollingLeaseReturnTierStore, ttl: Int, clock: Clock): RollingLeaseReturnTierStore {
    return CachedRollingLeaseReturnTierStore(
        inner = inner,
        cache = newTtlCache<RollingLeaseReturnTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling lease return tiers. Empty fields match all. */
data class RollingLeaseReturnTierQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingLeaseReturnTier): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingLeaseReturnTier>): MutableList<RollingLeaseReturnTier> {
        val matched = filter(items) { candidate: RollingLeaseReturnTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingLeaseReturnTiers(store: RollingLeaseReturnTierStore, query: RollingLeaseReturnTierQuery): MutableList<RollingLeaseReturnTier> {
    return query.apply(store.list())
}
