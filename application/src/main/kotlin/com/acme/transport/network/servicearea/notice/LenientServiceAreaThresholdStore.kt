package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient service area thresholds by code. */
interface LenientServiceAreaThresholdStore {
    fun find(code: String): LenientServiceAreaThreshold?
    fun save(item: LenientServiceAreaThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientServiceAreaThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientServiceAreaThresholdStore(
    var items: MutableMap<String, LenientServiceAreaThreshold>,
    var order: MutableList<String>,
) : LenientServiceAreaThresholdStore {
    override fun find(code: String): LenientServiceAreaThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientServiceAreaThreshold) {
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

    override fun list(): MutableList<LenientServiceAreaThreshold> {
        val out = mutableListOf<LenientServiceAreaThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientServiceAreaThresholdStore(): LenientServiceAreaThresholdStore {
    return MemoryLenientServiceAreaThresholdStore(
        mutableMapOf<String, LenientServiceAreaThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientServiceAreaThresholdStore(
    var inner: LenientServiceAreaThresholdStore,
    var cache: TtlCache<LenientServiceAreaThreshold>,
    var hits: Int,
    var misses: Int,
) : LenientServiceAreaThresholdStore {
    override fun find(code: String): LenientServiceAreaThreshold? {
        val hit = (cache.get(code) as LenientServiceAreaThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientServiceAreaThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientServiceAreaThreshold> = inner.list()
}

fun newCachedLenientServiceAreaThresholdStore(inner: LenientServiceAreaThresholdStore, ttl: Int, clock: Clock): LenientServiceAreaThresholdStore {
    return CachedLenientServiceAreaThresholdStore(
        inner = inner,
        cache = newTtlCache<LenientServiceAreaThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient service area thresholds. Empty fields match all. */
data class LenientServiceAreaThresholdQuery(
    var region: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientServiceAreaThreshold): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientServiceAreaThreshold>): MutableList<LenientServiceAreaThreshold> {
        val matched = filter(items) { candidate: LenientServiceAreaThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientServiceAreaThresholds(store: LenientServiceAreaThresholdStore, query: LenientServiceAreaThresholdQuery): MutableList<LenientServiceAreaThreshold> {
    return query.apply(store.list())
}
