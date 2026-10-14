package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient zone groups by code. */
interface LenientZoneGroupStore {
    fun find(code: String): LenientZoneGroup?
    fun save(item: LenientZoneGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientZoneGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientZoneGroupStore(
    var items: MutableMap<String, LenientZoneGroup>,
    var order: MutableList<String>,
) : LenientZoneGroupStore {
    override fun find(code: String): LenientZoneGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientZoneGroup) {
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

    override fun list(): MutableList<LenientZoneGroup> {
        val out = mutableListOf<LenientZoneGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientZoneGroupStore(): LenientZoneGroupStore {
    return MemoryLenientZoneGroupStore(mutableMapOf<String, LenientZoneGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientZoneGroupStore(
    var inner: LenientZoneGroupStore,
    var cache: TtlCache<LenientZoneGroup>,
    var hits: Int,
    var misses: Int,
) : LenientZoneGroupStore {
    override fun find(code: String): LenientZoneGroup? {
        val hit = (cache.get(code) as LenientZoneGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientZoneGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientZoneGroup> = inner.list()
}

fun newCachedLenientZoneGroupStore(inner: LenientZoneGroupStore, ttl: Int, clock: Clock): LenientZoneGroupStore {
    return CachedLenientZoneGroupStore(
        inner = inner,
        cache = newTtlCache<LenientZoneGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient zone groups. Empty fields match all. */
data class LenientZoneGroupQuery(
    var category: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientZoneGroup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientZoneGroup>): MutableList<LenientZoneGroup> {
        val matched = filter(items) { candidate: LenientZoneGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientZoneGroups(store: LenientZoneGroupStore, query: LenientZoneGroupQuery): MutableList<LenientZoneGroup> {
    return query.apply(store.list())
}
