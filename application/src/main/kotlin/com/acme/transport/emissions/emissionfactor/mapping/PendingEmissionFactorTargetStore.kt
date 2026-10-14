package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending emission factor targets by code. */
interface PendingEmissionFactorTargetStore {
    fun find(code: String): PendingEmissionFactorTarget?
    fun save(item: PendingEmissionFactorTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingEmissionFactorTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingEmissionFactorTargetStore(
    var items: MutableMap<String, PendingEmissionFactorTarget>,
    var order: MutableList<String>,
) : PendingEmissionFactorTargetStore {
    override fun find(code: String): PendingEmissionFactorTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingEmissionFactorTarget) {
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

    override fun list(): MutableList<PendingEmissionFactorTarget> {
        val out = mutableListOf<PendingEmissionFactorTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingEmissionFactorTargetStore(): PendingEmissionFactorTargetStore {
    return MemoryPendingEmissionFactorTargetStore(
        mutableMapOf<String, PendingEmissionFactorTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingEmissionFactorTargetStore(
    var inner: PendingEmissionFactorTargetStore,
    var cache: TtlCache<PendingEmissionFactorTarget>,
    var hits: Int,
    var misses: Int,
) : PendingEmissionFactorTargetStore {
    override fun find(code: String): PendingEmissionFactorTarget? {
        val hit = (cache.get(code) as PendingEmissionFactorTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingEmissionFactorTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingEmissionFactorTarget> = inner.list()
}

fun newCachedPendingEmissionFactorTargetStore(inner: PendingEmissionFactorTargetStore, ttl: Int, clock: Clock): PendingEmissionFactorTargetStore {
    return CachedPendingEmissionFactorTargetStore(
        inner = inner,
        cache = newTtlCache<PendingEmissionFactorTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending emission factor targets. Empty fields match all. */
data class PendingEmissionFactorTargetQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingEmissionFactorTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PendingEmissionFactorTarget>): MutableList<PendingEmissionFactorTarget> {
        val matched = filter(items) { candidate: PendingEmissionFactorTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingEmissionFactorTargets(store: PendingEmissionFactorTargetStore, query: PendingEmissionFactorTargetQuery): MutableList<PendingEmissionFactorTarget> {
    return query.apply(store.list())
}
