package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy lease return bundles by code. */
interface LegacyLeaseReturnBundleStore {
    fun find(code: String): LegacyLeaseReturnBundle?
    fun save(item: LegacyLeaseReturnBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLeaseReturnBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLeaseReturnBundleStore(
    var items: MutableMap<String, LegacyLeaseReturnBundle>,
    var order: MutableList<String>,
) : LegacyLeaseReturnBundleStore {
    override fun find(code: String): LegacyLeaseReturnBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLeaseReturnBundle) {
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

    override fun list(): MutableList<LegacyLeaseReturnBundle> {
        val out = mutableListOf<LegacyLeaseReturnBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLeaseReturnBundleStore(): LegacyLeaseReturnBundleStore {
    return MemoryLegacyLeaseReturnBundleStore(
        mutableMapOf<String, LegacyLeaseReturnBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLeaseReturnBundleStore(
    var inner: LegacyLeaseReturnBundleStore,
    var cache: TtlCache<LegacyLeaseReturnBundle>,
    var hits: Int,
    var misses: Int,
) : LegacyLeaseReturnBundleStore {
    override fun find(code: String): LegacyLeaseReturnBundle? {
        val hit = (cache.get(code) as LegacyLeaseReturnBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLeaseReturnBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLeaseReturnBundle> = inner.list()
}

fun newCachedLegacyLeaseReturnBundleStore(inner: LegacyLeaseReturnBundleStore, ttl: Int, clock: Clock): LegacyLeaseReturnBundleStore {
    return CachedLegacyLeaseReturnBundleStore(
        inner = inner,
        cache = newTtlCache<LegacyLeaseReturnBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy lease return bundles. Empty fields match all. */
data class LegacyLeaseReturnBundleQuery(
    var category: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLeaseReturnBundle): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyLeaseReturnBundle>): MutableList<LegacyLeaseReturnBundle> {
        val matched = filter(items) { candidate: LegacyLeaseReturnBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLeaseReturnBundles(store: LegacyLeaseReturnBundleStore, query: LegacyLeaseReturnBundleQuery): MutableList<LegacyLeaseReturnBundle> {
    return query.apply(store.list())
}
