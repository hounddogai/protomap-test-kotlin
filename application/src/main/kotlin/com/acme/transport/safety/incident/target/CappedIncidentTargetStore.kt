package com.acme.transport.safety.incident.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped incident targets by code. */
interface CappedIncidentTargetStore {
    fun find(code: String): CappedIncidentTarget?
    fun save(item: CappedIncidentTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedIncidentTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedIncidentTargetStore(
    var items: MutableMap<String, CappedIncidentTarget>,
    var order: MutableList<String>,
) : CappedIncidentTargetStore {
    override fun find(code: String): CappedIncidentTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedIncidentTarget) {
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

    override fun list(): MutableList<CappedIncidentTarget> {
        val out = mutableListOf<CappedIncidentTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedIncidentTargetStore(): CappedIncidentTargetStore {
    return MemoryCappedIncidentTargetStore(
        mutableMapOf<String, CappedIncidentTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedIncidentTargetStore(
    var inner: CappedIncidentTargetStore,
    var cache: TtlCache<CappedIncidentTarget>,
    var hits: Int,
    var misses: Int,
) : CappedIncidentTargetStore {
    override fun find(code: String): CappedIncidentTarget? {
        val hit = (cache.get(code) as CappedIncidentTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedIncidentTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedIncidentTarget> = inner.list()
}

fun newCachedCappedIncidentTargetStore(inner: CappedIncidentTargetStore, ttl: Int, clock: Clock): CappedIncidentTargetStore {
    return CachedCappedIncidentTargetStore(
        inner = inner,
        cache = newTtlCache<CappedIncidentTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped incident targets. Empty fields match all. */
data class CappedIncidentTargetQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedIncidentTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedIncidentTarget>): MutableList<CappedIncidentTarget> {
        val matched = filter(items) { candidate: CappedIncidentTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedIncidentTargets(store: CappedIncidentTargetStore, query: CappedIncidentTargetQuery): MutableList<CappedIncidentTarget> {
    return query.apply(store.list())
}
