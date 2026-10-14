package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global lease return overrides by code. */
interface GlobalLeaseReturnOverrideStore {
    fun find(code: String): GlobalLeaseReturnOverride?
    fun save(item: GlobalLeaseReturnOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalLeaseReturnOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalLeaseReturnOverrideStore(
    var items: MutableMap<String, GlobalLeaseReturnOverride>,
    var order: MutableList<String>,
) : GlobalLeaseReturnOverrideStore {
    override fun find(code: String): GlobalLeaseReturnOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalLeaseReturnOverride) {
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

    override fun list(): MutableList<GlobalLeaseReturnOverride> {
        val out = mutableListOf<GlobalLeaseReturnOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalLeaseReturnOverrideStore(): GlobalLeaseReturnOverrideStore {
    return MemoryGlobalLeaseReturnOverrideStore(
        mutableMapOf<String, GlobalLeaseReturnOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalLeaseReturnOverrideStore(
    var inner: GlobalLeaseReturnOverrideStore,
    var cache: TtlCache<GlobalLeaseReturnOverride>,
    var hits: Int,
    var misses: Int,
) : GlobalLeaseReturnOverrideStore {
    override fun find(code: String): GlobalLeaseReturnOverride? {
        val hit = (cache.get(code) as GlobalLeaseReturnOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalLeaseReturnOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalLeaseReturnOverride> = inner.list()
}

fun newCachedGlobalLeaseReturnOverrideStore(inner: GlobalLeaseReturnOverrideStore, ttl: Int, clock: Clock): GlobalLeaseReturnOverrideStore {
    return CachedGlobalLeaseReturnOverrideStore(
        inner = inner,
        cache = newTtlCache<GlobalLeaseReturnOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global lease return overrides. Empty fields match all. */
data class GlobalLeaseReturnOverrideQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalLeaseReturnOverride): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalLeaseReturnOverride>): MutableList<GlobalLeaseReturnOverride> {
        val matched = filter(items) { candidate: GlobalLeaseReturnOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalLeaseReturnOverrides(store: GlobalLeaseReturnOverrideStore, query: GlobalLeaseReturnOverrideQuery): MutableList<GlobalLeaseReturnOverride> {
    return query.apply(store.list())
}
