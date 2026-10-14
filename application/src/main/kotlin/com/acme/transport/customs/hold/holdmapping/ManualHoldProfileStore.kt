package com.acme.transport.customs.hold.holdmapping

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual hold profiles by code. */
interface ManualHoldProfileStore {
    fun find(code: String): ManualHoldProfile?
    fun save(item: ManualHoldProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualHoldProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualHoldProfileStore(
    var items: MutableMap<String, ManualHoldProfile>,
    var order: MutableList<String>,
) : ManualHoldProfileStore {
    override fun find(code: String): ManualHoldProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualHoldProfile) {
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

    override fun list(): MutableList<ManualHoldProfile> {
        val out = mutableListOf<ManualHoldProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualHoldProfileStore(): ManualHoldProfileStore {
    return MemoryManualHoldProfileStore(mutableMapOf<String, ManualHoldProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualHoldProfileStore(
    var inner: ManualHoldProfileStore,
    var cache: TtlCache<ManualHoldProfile>,
    var hits: Int,
    var misses: Int,
) : ManualHoldProfileStore {
    override fun find(code: String): ManualHoldProfile? {
        val hit = (cache.get(code) as ManualHoldProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualHoldProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualHoldProfile> = inner.list()
}

fun newCachedManualHoldProfileStore(inner: ManualHoldProfileStore, ttl: Int, clock: Clock): ManualHoldProfileStore {
    return CachedManualHoldProfileStore(
        inner = inner,
        cache = newTtlCache<ManualHoldProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual hold profiles. Empty fields match all. */
data class ManualHoldProfileQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualHoldProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualHoldProfile>): MutableList<ManualHoldProfile> {
        val matched = filter(items) { candidate: ManualHoldProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualHoldProfiles(store: ManualHoldProfileStore, query: ManualHoldProfileQuery): MutableList<ManualHoldProfile> {
    return query.apply(store.list())
}
