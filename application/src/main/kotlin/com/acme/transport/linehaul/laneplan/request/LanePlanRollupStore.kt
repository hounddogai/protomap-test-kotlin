package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan rollups by code. */
interface LanePlanRollupStore {
    fun find(code: String): LanePlanRollup?
    fun save(item: LanePlanRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanRollupStore(
    var items: MutableMap<String, LanePlanRollup>,
    var order: MutableList<String>,
) : LanePlanRollupStore {
    override fun find(code: String): LanePlanRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanRollup) {
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

    override fun list(): MutableList<LanePlanRollup> {
        val out = mutableListOf<LanePlanRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanRollupStore(): LanePlanRollupStore {
    return MemoryLanePlanRollupStore(mutableMapOf<String, LanePlanRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanRollupStore(
    var inner: LanePlanRollupStore,
    var cache: TtlCache<LanePlanRollup>,
    var hits: Int,
    var misses: Int,
) : LanePlanRollupStore {
    override fun find(code: String): LanePlanRollup? {
        val hit = (cache.get(code) as LanePlanRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanRollup> = inner.list()
}

fun newCachedLanePlanRollupStore(inner: LanePlanRollupStore, ttl: Int, clock: Clock): LanePlanRollupStore {
    return CachedLanePlanRollupStore(
        inner = inner,
        cache = newTtlCache<LanePlanRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan rollups. Empty fields match all. */
data class LanePlanRollupQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanRollup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LanePlanRollup>): MutableList<LanePlanRollup> {
        val matched = filter(items) { candidate: LanePlanRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanRollups(store: LanePlanRollupStore, query: LanePlanRollupQuery): MutableList<LanePlanRollup> {
    return query.apply(store.list())
}
