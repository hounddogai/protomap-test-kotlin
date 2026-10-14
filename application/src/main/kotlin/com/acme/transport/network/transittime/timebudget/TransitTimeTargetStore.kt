package com.acme.transport.network.transittime.timebudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time targets by code. */
interface TransitTimeTargetStore {
    fun find(code: String): TransitTimeTarget?
    fun save(item: TransitTimeTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeTargetStore(
    var items: MutableMap<String, TransitTimeTarget>,
    var order: MutableList<String>,
) : TransitTimeTargetStore {
    override fun find(code: String): TransitTimeTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeTarget) {
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

    override fun list(): MutableList<TransitTimeTarget> {
        val out = mutableListOf<TransitTimeTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeTargetStore(): TransitTimeTargetStore {
    return MemoryTransitTimeTargetStore(mutableMapOf<String, TransitTimeTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeTargetStore(
    var inner: TransitTimeTargetStore,
    var cache: TtlCache<TransitTimeTarget>,
    var hits: Int,
    var misses: Int,
) : TransitTimeTargetStore {
    override fun find(code: String): TransitTimeTarget? {
        val hit = (cache.get(code) as TransitTimeTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeTarget> = inner.list()
}

fun newCachedTransitTimeTargetStore(inner: TransitTimeTargetStore, ttl: Int, clock: Clock): TransitTimeTargetStore {
    return CachedTransitTimeTargetStore(
        inner = inner,
        cache = newTtlCache<TransitTimeTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time targets. Empty fields match all. */
data class TransitTimeTargetQuery(
    var unit: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeTarget): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeTarget>): MutableList<TransitTimeTarget> {
        val matched = filter(items) { candidate: TransitTimeTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeTargets(store: TransitTimeTargetStore, query: TransitTimeTargetQuery): MutableList<TransitTimeTarget> {
    return query.apply(store.list())
}
