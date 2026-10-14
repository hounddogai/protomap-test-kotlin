package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone plans by code. */
interface ZonePlanStore {
    fun find(code: String): ZonePlan?
    fun save(item: ZonePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZonePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZonePlanStore(
    var items: MutableMap<String, ZonePlan>,
    var order: MutableList<String>,
) : ZonePlanStore {
    override fun find(code: String): ZonePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZonePlan) {
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

    override fun list(): MutableList<ZonePlan> {
        val out = mutableListOf<ZonePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZonePlanStore(): ZonePlanStore {
    return MemoryZonePlanStore(mutableMapOf<String, ZonePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZonePlanStore(
    var inner: ZonePlanStore,
    var cache: TtlCache<ZonePlan>,
    var hits: Int,
    var misses: Int,
) : ZonePlanStore {
    override fun find(code: String): ZonePlan? {
        val hit = (cache.get(code) as ZonePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZonePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZonePlan> = inner.list()
}

fun newCachedZonePlanStore(inner: ZonePlanStore, ttl: Int, clock: Clock): ZonePlanStore {
    return CachedZonePlanStore(
        inner = inner,
        cache = newTtlCache<ZonePlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone plans. Empty fields match all. */
data class ZonePlanQuery(
    var locale: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZonePlan): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<ZonePlan>): MutableList<ZonePlan> {
        val matched = filter(items) { candidate: ZonePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZonePlans(store: ZonePlanStore, query: ZonePlanQuery): MutableList<ZonePlan> = query.apply(store.list())
