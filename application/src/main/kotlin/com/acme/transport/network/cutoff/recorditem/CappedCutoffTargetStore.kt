package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped cutoff targets by code. */
interface CappedCutoffTargetStore {
    fun find(code: String): CappedCutoffTarget?
    fun save(item: CappedCutoffTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCutoffTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCutoffTargetStore(
    var items: MutableMap<String, CappedCutoffTarget>,
    var order: MutableList<String>,
) : CappedCutoffTargetStore {
    override fun find(code: String): CappedCutoffTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCutoffTarget) {
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

    override fun list(): MutableList<CappedCutoffTarget> {
        val out = mutableListOf<CappedCutoffTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCutoffTargetStore(): CappedCutoffTargetStore {
    return MemoryCappedCutoffTargetStore(mutableMapOf<String, CappedCutoffTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCutoffTargetStore(
    var inner: CappedCutoffTargetStore,
    var cache: TtlCache<CappedCutoffTarget>,
    var hits: Int,
    var misses: Int,
) : CappedCutoffTargetStore {
    override fun find(code: String): CappedCutoffTarget? {
        val hit = (cache.get(code) as CappedCutoffTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCutoffTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCutoffTarget> = inner.list()
}

fun newCachedCappedCutoffTargetStore(inner: CappedCutoffTargetStore, ttl: Int, clock: Clock): CappedCutoffTargetStore {
    return CachedCappedCutoffTargetStore(
        inner = inner,
        cache = newTtlCache<CappedCutoffTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped cutoff targets. Empty fields match all. */
data class CappedCutoffTargetQuery(
    var region: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCutoffTarget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCutoffTarget>): MutableList<CappedCutoffTarget> {
        val matched = filter(items) { candidate: CappedCutoffTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCutoffTargets(store: CappedCutoffTargetStore, query: CappedCutoffTargetQuery): MutableList<CappedCutoffTarget> {
    return query.apply(store.list())
}
