package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual lane plan targets by code. */
interface ManualLanePlanTargetStore {
    fun find(code: String): ManualLanePlanTarget?
    fun save(item: ManualLanePlanTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualLanePlanTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualLanePlanTargetStore(
    var items: MutableMap<String, ManualLanePlanTarget>,
    var order: MutableList<String>,
) : ManualLanePlanTargetStore {
    override fun find(code: String): ManualLanePlanTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualLanePlanTarget) {
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

    override fun list(): MutableList<ManualLanePlanTarget> {
        val out = mutableListOf<ManualLanePlanTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualLanePlanTargetStore(): ManualLanePlanTargetStore {
    return MemoryManualLanePlanTargetStore(
        mutableMapOf<String, ManualLanePlanTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualLanePlanTargetStore(
    var inner: ManualLanePlanTargetStore,
    var cache: TtlCache<ManualLanePlanTarget>,
    var hits: Int,
    var misses: Int,
) : ManualLanePlanTargetStore {
    override fun find(code: String): ManualLanePlanTarget? {
        val hit = (cache.get(code) as ManualLanePlanTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualLanePlanTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualLanePlanTarget> = inner.list()
}

fun newCachedManualLanePlanTargetStore(inner: ManualLanePlanTargetStore, ttl: Int, clock: Clock): ManualLanePlanTargetStore {
    return CachedManualLanePlanTargetStore(
        inner = inner,
        cache = newTtlCache<ManualLanePlanTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual lane plan targets. Empty fields match all. */
data class ManualLanePlanTargetQuery(
    var depot: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualLanePlanTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualLanePlanTarget>): MutableList<ManualLanePlanTarget> {
        val matched = filter(items) { candidate: ManualLanePlanTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualLanePlanTargets(store: ManualLanePlanTargetStore, query: ManualLanePlanTargetQuery): MutableList<ManualLanePlanTarget> {
    return query.apply(store.list())
}
