package com.acme.transport.network.zone.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global zone targets by code. */
interface GlobalZoneTargetStore {
    fun find(code: String): GlobalZoneTarget?
    fun save(item: GlobalZoneTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalZoneTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalZoneTargetStore(
    var items: MutableMap<String, GlobalZoneTarget>,
    var order: MutableList<String>,
) : GlobalZoneTargetStore {
    override fun find(code: String): GlobalZoneTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalZoneTarget) {
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

    override fun list(): MutableList<GlobalZoneTarget> {
        val out = mutableListOf<GlobalZoneTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalZoneTargetStore(): GlobalZoneTargetStore {
    return MemoryGlobalZoneTargetStore(mutableMapOf<String, GlobalZoneTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalZoneTargetStore(
    var inner: GlobalZoneTargetStore,
    var cache: TtlCache<GlobalZoneTarget>,
    var hits: Int,
    var misses: Int,
) : GlobalZoneTargetStore {
    override fun find(code: String): GlobalZoneTarget? {
        val hit = (cache.get(code) as GlobalZoneTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalZoneTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalZoneTarget> = inner.list()
}

fun newCachedGlobalZoneTargetStore(inner: GlobalZoneTargetStore, ttl: Int, clock: Clock): GlobalZoneTargetStore {
    return CachedGlobalZoneTargetStore(
        inner = inner,
        cache = newTtlCache<GlobalZoneTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global zone targets. Empty fields match all. */
data class GlobalZoneTargetQuery(
    var locale: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalZoneTarget): Boolean {
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

    fun apply(items: MutableList<GlobalZoneTarget>): MutableList<GlobalZoneTarget> {
        val matched = filter(items) { candidate: GlobalZoneTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalZoneTargets(store: GlobalZoneTargetStore, query: GlobalZoneTargetQuery): MutableList<GlobalZoneTarget> {
    return query.apply(store.list())
}
