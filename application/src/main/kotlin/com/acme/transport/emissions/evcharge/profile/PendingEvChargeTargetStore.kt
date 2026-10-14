package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending ev charge targets by code. */
interface PendingEvChargeTargetStore {
    fun find(code: String): PendingEvChargeTarget?
    fun save(item: PendingEvChargeTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingEvChargeTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingEvChargeTargetStore(
    var items: MutableMap<String, PendingEvChargeTarget>,
    var order: MutableList<String>,
) : PendingEvChargeTargetStore {
    override fun find(code: String): PendingEvChargeTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingEvChargeTarget) {
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

    override fun list(): MutableList<PendingEvChargeTarget> {
        val out = mutableListOf<PendingEvChargeTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingEvChargeTargetStore(): PendingEvChargeTargetStore {
    return MemoryPendingEvChargeTargetStore(
        mutableMapOf<String, PendingEvChargeTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingEvChargeTargetStore(
    var inner: PendingEvChargeTargetStore,
    var cache: TtlCache<PendingEvChargeTarget>,
    var hits: Int,
    var misses: Int,
) : PendingEvChargeTargetStore {
    override fun find(code: String): PendingEvChargeTarget? {
        val hit = (cache.get(code) as PendingEvChargeTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingEvChargeTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingEvChargeTarget> = inner.list()
}

fun newCachedPendingEvChargeTargetStore(inner: PendingEvChargeTargetStore, ttl: Int, clock: Clock): PendingEvChargeTargetStore {
    return CachedPendingEvChargeTargetStore(
        inner = inner,
        cache = newTtlCache<PendingEvChargeTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending ev charge targets. Empty fields match all. */
data class PendingEvChargeTargetQuery(
    var category: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingEvChargeTarget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingEvChargeTarget>): MutableList<PendingEvChargeTarget> {
        val matched = filter(items) { candidate: PendingEvChargeTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingEvChargeTargets(store: PendingEvChargeTargetStore, query: PendingEvChargeTargetQuery): MutableList<PendingEvChargeTarget> {
    return query.apply(store.list())
}
