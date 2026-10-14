package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy lane plan targets by code. */
interface LegacyLanePlanTargetStore {
    fun find(code: String): LegacyLanePlanTarget?
    fun save(item: LegacyLanePlanTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLanePlanTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLanePlanTargetStore(
    var items: MutableMap<String, LegacyLanePlanTarget>,
    var order: MutableList<String>,
) : LegacyLanePlanTargetStore {
    override fun find(code: String): LegacyLanePlanTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLanePlanTarget) {
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

    override fun list(): MutableList<LegacyLanePlanTarget> {
        val out = mutableListOf<LegacyLanePlanTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLanePlanTargetStore(): LegacyLanePlanTargetStore {
    return MemoryLegacyLanePlanTargetStore(
        mutableMapOf<String, LegacyLanePlanTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLanePlanTargetStore(
    var inner: LegacyLanePlanTargetStore,
    var cache: TtlCache<LegacyLanePlanTarget>,
    var hits: Int,
    var misses: Int,
) : LegacyLanePlanTargetStore {
    override fun find(code: String): LegacyLanePlanTarget? {
        val hit = (cache.get(code) as LegacyLanePlanTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLanePlanTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLanePlanTarget> = inner.list()
}

fun newCachedLegacyLanePlanTargetStore(inner: LegacyLanePlanTargetStore, ttl: Int, clock: Clock): LegacyLanePlanTargetStore {
    return CachedLegacyLanePlanTargetStore(
        inner = inner,
        cache = newTtlCache<LegacyLanePlanTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy lane plan targets. Empty fields match all. */
data class LegacyLanePlanTargetQuery(
    var segment: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLanePlanTarget): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyLanePlanTarget>): MutableList<LegacyLanePlanTarget> {
        val matched = filter(items) { candidate: LegacyLanePlanTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLanePlanTargets(store: LegacyLanePlanTargetStore, query: LegacyLanePlanTargetQuery): MutableList<LegacyLanePlanTarget> {
    return query.apply(store.list())
}
