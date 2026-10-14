package com.acme.transport.customs.hold.summary

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold profiles by code. */
interface HoldProfileStore {
    fun find(code: String): HoldProfile?
    fun save(item: HoldProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldProfileStore(
    var items: MutableMap<String, HoldProfile>,
    var order: MutableList<String>,
) : HoldProfileStore {
    override fun find(code: String): HoldProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldProfile) {
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

    override fun list(): MutableList<HoldProfile> {
        val out = mutableListOf<HoldProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldProfileStore(): HoldProfileStore {
    return MemoryHoldProfileStore(mutableMapOf<String, HoldProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldProfileStore(
    var inner: HoldProfileStore,
    var cache: TtlCache<HoldProfile>,
    var hits: Int,
    var misses: Int,
) : HoldProfileStore {
    override fun find(code: String): HoldProfile? {
        val hit = (cache.get(code) as HoldProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldProfile> = inner.list()
}

fun newCachedHoldProfileStore(inner: HoldProfileStore, ttl: Int, clock: Clock): HoldProfileStore {
    return CachedHoldProfileStore(
        inner = inner,
        cache = newTtlCache<HoldProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold profiles. Empty fields match all. */
data class HoldProfileQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<HoldProfile>): MutableList<HoldProfile> {
        val matched = filter(items) { candidate: HoldProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldProfiles(store: HoldProfileStore, query: HoldProfileQuery): MutableList<HoldProfile> {
    return query.apply(store.list())
}
