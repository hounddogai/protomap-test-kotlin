package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled lane plan overrides by code. */
interface ScheduledLanePlanOverrideStore {
    fun find(code: String): ScheduledLanePlanOverride?
    fun save(item: ScheduledLanePlanOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLanePlanOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLanePlanOverrideStore(
    var items: MutableMap<String, ScheduledLanePlanOverride>,
    var order: MutableList<String>,
) : ScheduledLanePlanOverrideStore {
    override fun find(code: String): ScheduledLanePlanOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLanePlanOverride) {
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

    override fun list(): MutableList<ScheduledLanePlanOverride> {
        val out = mutableListOf<ScheduledLanePlanOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLanePlanOverrideStore(): ScheduledLanePlanOverrideStore {
    return MemoryScheduledLanePlanOverrideStore(
        mutableMapOf<String, ScheduledLanePlanOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLanePlanOverrideStore(
    var inner: ScheduledLanePlanOverrideStore,
    var cache: TtlCache<ScheduledLanePlanOverride>,
    var hits: Int,
    var misses: Int,
) : ScheduledLanePlanOverrideStore {
    override fun find(code: String): ScheduledLanePlanOverride? {
        val hit = (cache.get(code) as ScheduledLanePlanOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLanePlanOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLanePlanOverride> = inner.list()
}

fun newCachedScheduledLanePlanOverrideStore(inner: ScheduledLanePlanOverrideStore, ttl: Int, clock: Clock): ScheduledLanePlanOverrideStore {
    return CachedScheduledLanePlanOverrideStore(
        inner = inner,
        cache = newTtlCache<ScheduledLanePlanOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled lane plan overrides. Empty fields match all. */
data class ScheduledLanePlanOverrideQuery(
    var unit: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLanePlanOverride): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLanePlanOverride>): MutableList<ScheduledLanePlanOverride> {
        val matched = filter(items) { candidate: ScheduledLanePlanOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLanePlanOverrides(store: ScheduledLanePlanOverrideStore, query: ScheduledLanePlanOverrideQuery): MutableList<ScheduledLanePlanOverride> {
    return query.apply(store.list())
}
