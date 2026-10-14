package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional cutoff targets by code. */
interface RegionalCutoffTargetStore {
    fun find(code: String): RegionalCutoffTarget?
    fun save(item: RegionalCutoffTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCutoffTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCutoffTargetStore(
    var items: MutableMap<String, RegionalCutoffTarget>,
    var order: MutableList<String>,
) : RegionalCutoffTargetStore {
    override fun find(code: String): RegionalCutoffTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCutoffTarget) {
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

    override fun list(): MutableList<RegionalCutoffTarget> {
        val out = mutableListOf<RegionalCutoffTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCutoffTargetStore(): RegionalCutoffTargetStore {
    return MemoryRegionalCutoffTargetStore(
        mutableMapOf<String, RegionalCutoffTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCutoffTargetStore(
    var inner: RegionalCutoffTargetStore,
    var cache: TtlCache<RegionalCutoffTarget>,
    var hits: Int,
    var misses: Int,
) : RegionalCutoffTargetStore {
    override fun find(code: String): RegionalCutoffTarget? {
        val hit = (cache.get(code) as RegionalCutoffTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCutoffTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCutoffTarget> = inner.list()
}

fun newCachedRegionalCutoffTargetStore(inner: RegionalCutoffTargetStore, ttl: Int, clock: Clock): RegionalCutoffTargetStore {
    return CachedRegionalCutoffTargetStore(
        inner = inner,
        cache = newTtlCache<RegionalCutoffTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional cutoff targets. Empty fields match all. */
data class RegionalCutoffTargetQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCutoffTarget): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCutoffTarget>): MutableList<RegionalCutoffTarget> {
        val matched = filter(items) { candidate: RegionalCutoffTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCutoffTargets(store: RegionalCutoffTargetStore, query: RegionalCutoffTargetQuery): MutableList<RegionalCutoffTarget> {
    return query.apply(store.list())
}
