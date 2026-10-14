package com.acme.transport.fleetops.leasereturn.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return targets by code. */
interface LeaseReturnTargetStore {
    fun find(code: String): LeaseReturnTarget?
    fun save(item: LeaseReturnTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnTargetStore(
    var items: MutableMap<String, LeaseReturnTarget>,
    var order: MutableList<String>,
) : LeaseReturnTargetStore {
    override fun find(code: String): LeaseReturnTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnTarget) {
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

    override fun list(): MutableList<LeaseReturnTarget> {
        val out = mutableListOf<LeaseReturnTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnTargetStore(): LeaseReturnTargetStore {
    return MemoryLeaseReturnTargetStore(mutableMapOf<String, LeaseReturnTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnTargetStore(
    var inner: LeaseReturnTargetStore,
    var cache: TtlCache<LeaseReturnTarget>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnTargetStore {
    override fun find(code: String): LeaseReturnTarget? {
        val hit = (cache.get(code) as LeaseReturnTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnTarget> = inner.list()
}

fun newCachedLeaseReturnTargetStore(inner: LeaseReturnTargetStore, ttl: Int, clock: Clock): LeaseReturnTargetStore {
    return CachedLeaseReturnTargetStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return targets. Empty fields match all. */
data class LeaseReturnTargetQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnTarget): Boolean {
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

    fun apply(items: MutableList<LeaseReturnTarget>): MutableList<LeaseReturnTarget> {
        val matched = filter(items) { candidate: LeaseReturnTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnTargets(store: LeaseReturnTargetStore, query: LeaseReturnTargetQuery): MutableList<LeaseReturnTarget> {
    return query.apply(store.list())
}
