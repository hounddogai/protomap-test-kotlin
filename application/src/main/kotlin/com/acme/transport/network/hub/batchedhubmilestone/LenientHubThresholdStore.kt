package com.acme.transport.network.hub.batchedhubmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient hub thresholds by code. */
interface LenientHubThresholdStore {
    fun find(code: String): LenientHubThreshold?
    fun save(item: LenientHubThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientHubThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientHubThresholdStore(
    var items: MutableMap<String, LenientHubThreshold>,
    var order: MutableList<String>,
) : LenientHubThresholdStore {
    override fun find(code: String): LenientHubThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientHubThreshold) {
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

    override fun list(): MutableList<LenientHubThreshold> {
        val out = mutableListOf<LenientHubThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientHubThresholdStore(): LenientHubThresholdStore {
    return MemoryLenientHubThresholdStore(mutableMapOf<String, LenientHubThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientHubThresholdStore(
    var inner: LenientHubThresholdStore,
    var cache: TtlCache<LenientHubThreshold>,
    var hits: Int,
    var misses: Int,
) : LenientHubThresholdStore {
    override fun find(code: String): LenientHubThreshold? {
        val hit = (cache.get(code) as LenientHubThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientHubThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientHubThreshold> = inner.list()
}

fun newCachedLenientHubThresholdStore(inner: LenientHubThresholdStore, ttl: Int, clock: Clock): LenientHubThresholdStore {
    return CachedLenientHubThresholdStore(
        inner = inner,
        cache = newTtlCache<LenientHubThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient hub thresholds. Empty fields match all. */
data class LenientHubThresholdQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientHubThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientHubThreshold>): MutableList<LenientHubThreshold> {
        val matched = filter(items) { candidate: LenientHubThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientHubThresholds(store: LenientHubThresholdStore, query: LenientHubThresholdQuery): MutableList<LenientHubThreshold> {
    return query.apply(store.list())
}
