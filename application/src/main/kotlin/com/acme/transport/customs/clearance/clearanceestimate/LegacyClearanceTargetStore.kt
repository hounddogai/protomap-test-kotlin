package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy clearance targets by code. */
interface LegacyClearanceTargetStore {
    fun find(code: String): LegacyClearanceTarget?
    fun save(item: LegacyClearanceTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyClearanceTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyClearanceTargetStore(
    var items: MutableMap<String, LegacyClearanceTarget>,
    var order: MutableList<String>,
) : LegacyClearanceTargetStore {
    override fun find(code: String): LegacyClearanceTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyClearanceTarget) {
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

    override fun list(): MutableList<LegacyClearanceTarget> {
        val out = mutableListOf<LegacyClearanceTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyClearanceTargetStore(): LegacyClearanceTargetStore {
    return MemoryLegacyClearanceTargetStore(
        mutableMapOf<String, LegacyClearanceTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyClearanceTargetStore(
    var inner: LegacyClearanceTargetStore,
    var cache: TtlCache<LegacyClearanceTarget>,
    var hits: Int,
    var misses: Int,
) : LegacyClearanceTargetStore {
    override fun find(code: String): LegacyClearanceTarget? {
        val hit = (cache.get(code) as LegacyClearanceTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyClearanceTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyClearanceTarget> = inner.list()
}

fun newCachedLegacyClearanceTargetStore(inner: LegacyClearanceTargetStore, ttl: Int, clock: Clock): LegacyClearanceTargetStore {
    return CachedLegacyClearanceTargetStore(
        inner = inner,
        cache = newTtlCache<LegacyClearanceTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy clearance targets. Empty fields match all. */
data class LegacyClearanceTargetQuery(
    var locale: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyClearanceTarget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<LegacyClearanceTarget>): MutableList<LegacyClearanceTarget> {
        val matched = filter(items) { candidate: LegacyClearanceTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyClearanceTargets(store: LegacyClearanceTargetStore, query: LegacyClearanceTargetQuery): MutableList<LegacyClearanceTarget> {
    return query.apply(store.list())
}
