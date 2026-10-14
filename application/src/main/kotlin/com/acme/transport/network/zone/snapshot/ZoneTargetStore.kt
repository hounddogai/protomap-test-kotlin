package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone targets by code. */
interface ZoneTargetStore {
    fun find(code: String): ZoneTarget?
    fun save(item: ZoneTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneTargetStore(
    var items: MutableMap<String, ZoneTarget>,
    var order: MutableList<String>,
) : ZoneTargetStore {
    override fun find(code: String): ZoneTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneTarget) {
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

    override fun list(): MutableList<ZoneTarget> {
        val out = mutableListOf<ZoneTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneTargetStore(): ZoneTargetStore {
    return MemoryZoneTargetStore(mutableMapOf<String, ZoneTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneTargetStore(
    var inner: ZoneTargetStore,
    var cache: TtlCache<ZoneTarget>,
    var hits: Int,
    var misses: Int,
) : ZoneTargetStore {
    override fun find(code: String): ZoneTarget? {
        val hit = (cache.get(code) as ZoneTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneTarget> = inner.list()
}

fun newCachedZoneTargetStore(inner: ZoneTargetStore, ttl: Int, clock: Clock): ZoneTargetStore {
    return CachedZoneTargetStore(
        inner = inner,
        cache = newTtlCache<ZoneTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone targets. Empty fields match all. */
data class ZoneTargetQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneTarget>): MutableList<ZoneTarget> {
        val matched = filter(items) { candidate: ZoneTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneTargets(store: ZoneTargetStore, query: ZoneTargetQuery): MutableList<ZoneTarget> {
    return query.apply(store.list())
}
