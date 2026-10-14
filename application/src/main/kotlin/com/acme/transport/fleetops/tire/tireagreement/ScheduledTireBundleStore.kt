package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled tire bundles by code. */
interface ScheduledTireBundleStore {
    fun find(code: String): ScheduledTireBundle?
    fun save(item: ScheduledTireBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTireBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTireBundleStore(
    var items: MutableMap<String, ScheduledTireBundle>,
    var order: MutableList<String>,
) : ScheduledTireBundleStore {
    override fun find(code: String): ScheduledTireBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTireBundle) {
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

    override fun list(): MutableList<ScheduledTireBundle> {
        val out = mutableListOf<ScheduledTireBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTireBundleStore(): ScheduledTireBundleStore {
    return MemoryScheduledTireBundleStore(mutableMapOf<String, ScheduledTireBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTireBundleStore(
    var inner: ScheduledTireBundleStore,
    var cache: TtlCache<ScheduledTireBundle>,
    var hits: Int,
    var misses: Int,
) : ScheduledTireBundleStore {
    override fun find(code: String): ScheduledTireBundle? {
        val hit = (cache.get(code) as ScheduledTireBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTireBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTireBundle> = inner.list()
}

fun newCachedScheduledTireBundleStore(inner: ScheduledTireBundleStore, ttl: Int, clock: Clock): ScheduledTireBundleStore {
    return CachedScheduledTireBundleStore(
        inner = inner,
        cache = newTtlCache<ScheduledTireBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled tire bundles. Empty fields match all. */
data class ScheduledTireBundleQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTireBundle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTireBundle>): MutableList<ScheduledTireBundle> {
        val matched = filter(items) { candidate: ScheduledTireBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTireBundles(store: ScheduledTireBundleStore, query: ScheduledTireBundleQuery): MutableList<ScheduledTireBundle> {
    return query.apply(store.list())
}
