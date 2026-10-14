package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending locker overrides by code. */
interface PendingLockerOverrideStore {
    fun find(code: String): PendingLockerOverride?
    fun save(item: PendingLockerOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingLockerOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingLockerOverrideStore(
    var items: MutableMap<String, PendingLockerOverride>,
    var order: MutableList<String>,
) : PendingLockerOverrideStore {
    override fun find(code: String): PendingLockerOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingLockerOverride) {
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

    override fun list(): MutableList<PendingLockerOverride> {
        val out = mutableListOf<PendingLockerOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingLockerOverrideStore(): PendingLockerOverrideStore {
    return MemoryPendingLockerOverrideStore(
        mutableMapOf<String, PendingLockerOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingLockerOverrideStore(
    var inner: PendingLockerOverrideStore,
    var cache: TtlCache<PendingLockerOverride>,
    var hits: Int,
    var misses: Int,
) : PendingLockerOverrideStore {
    override fun find(code: String): PendingLockerOverride? {
        val hit = (cache.get(code) as PendingLockerOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingLockerOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingLockerOverride> = inner.list()
}

fun newCachedPendingLockerOverrideStore(inner: PendingLockerOverrideStore, ttl: Int, clock: Clock): PendingLockerOverrideStore {
    return CachedPendingLockerOverrideStore(
        inner = inner,
        cache = newTtlCache<PendingLockerOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending locker overrides. Empty fields match all. */
data class PendingLockerOverrideQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingLockerOverride): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingLockerOverride>): MutableList<PendingLockerOverride> {
        val matched = filter(items) { candidate: PendingLockerOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingLockerOverrides(store: PendingLockerOverrideStore, query: PendingLockerOverrideQuery): MutableList<PendingLockerOverride> {
    return query.apply(store.list())
}
