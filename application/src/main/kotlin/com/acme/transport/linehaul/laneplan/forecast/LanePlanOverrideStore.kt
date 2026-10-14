package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan overrides by code. */
interface LanePlanOverrideStore {
    fun find(code: String): LanePlanOverride?
    fun save(item: LanePlanOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanOverrideStore(
    var items: MutableMap<String, LanePlanOverride>,
    var order: MutableList<String>,
) : LanePlanOverrideStore {
    override fun find(code: String): LanePlanOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanOverride) {
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

    override fun list(): MutableList<LanePlanOverride> {
        val out = mutableListOf<LanePlanOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanOverrideStore(): LanePlanOverrideStore {
    return MemoryLanePlanOverrideStore(mutableMapOf<String, LanePlanOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanOverrideStore(
    var inner: LanePlanOverrideStore,
    var cache: TtlCache<LanePlanOverride>,
    var hits: Int,
    var misses: Int,
) : LanePlanOverrideStore {
    override fun find(code: String): LanePlanOverride? {
        val hit = (cache.get(code) as LanePlanOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanOverride> = inner.list()
}

fun newCachedLanePlanOverrideStore(inner: LanePlanOverrideStore, ttl: Int, clock: Clock): LanePlanOverrideStore {
    return CachedLanePlanOverrideStore(
        inner = inner,
        cache = newTtlCache<LanePlanOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan overrides. Empty fields match all. */
data class LanePlanOverrideQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanOverride): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LanePlanOverride>): MutableList<LanePlanOverride> {
        val matched = filter(items) { candidate: LanePlanOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanOverrides(store: LanePlanOverrideStore, query: LanePlanOverrideQuery): MutableList<LanePlanOverride> {
    return query.apply(store.list())
}
