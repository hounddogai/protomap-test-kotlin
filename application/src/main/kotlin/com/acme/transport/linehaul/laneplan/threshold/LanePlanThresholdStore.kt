package com.acme.transport.linehaul.laneplan.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan thresholds by code. */
interface LanePlanThresholdStore {
    fun find(code: String): LanePlanThreshold?
    fun save(item: LanePlanThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanThresholdStore(
    var items: MutableMap<String, LanePlanThreshold>,
    var order: MutableList<String>,
) : LanePlanThresholdStore {
    override fun find(code: String): LanePlanThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanThreshold) {
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

    override fun list(): MutableList<LanePlanThreshold> {
        val out = mutableListOf<LanePlanThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanThresholdStore(): LanePlanThresholdStore {
    return MemoryLanePlanThresholdStore(mutableMapOf<String, LanePlanThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanThresholdStore(
    var inner: LanePlanThresholdStore,
    var cache: TtlCache<LanePlanThreshold>,
    var hits: Int,
    var misses: Int,
) : LanePlanThresholdStore {
    override fun find(code: String): LanePlanThreshold? {
        val hit = (cache.get(code) as LanePlanThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanThreshold> = inner.list()
}

fun newCachedLanePlanThresholdStore(inner: LanePlanThresholdStore, ttl: Int, clock: Clock): LanePlanThresholdStore {
    return CachedLanePlanThresholdStore(
        inner = inner,
        cache = newTtlCache<LanePlanThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan thresholds. Empty fields match all. */
data class LanePlanThresholdQuery(
    var locale: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanThreshold): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanThreshold>): MutableList<LanePlanThreshold> {
        val matched = filter(items) { candidate: LanePlanThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanThresholds(store: LanePlanThresholdStore, query: LanePlanThresholdQuery): MutableList<LanePlanThreshold> {
    return query.apply(store.list())
}
