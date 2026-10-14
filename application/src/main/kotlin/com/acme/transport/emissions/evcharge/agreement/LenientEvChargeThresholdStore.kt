package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient ev charge thresholds by code. */
interface LenientEvChargeThresholdStore {
    fun find(code: String): LenientEvChargeThreshold?
    fun save(item: LenientEvChargeThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientEvChargeThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientEvChargeThresholdStore(
    var items: MutableMap<String, LenientEvChargeThreshold>,
    var order: MutableList<String>,
) : LenientEvChargeThresholdStore {
    override fun find(code: String): LenientEvChargeThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientEvChargeThreshold) {
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

    override fun list(): MutableList<LenientEvChargeThreshold> {
        val out = mutableListOf<LenientEvChargeThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientEvChargeThresholdStore(): LenientEvChargeThresholdStore {
    return MemoryLenientEvChargeThresholdStore(
        mutableMapOf<String, LenientEvChargeThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientEvChargeThresholdStore(
    var inner: LenientEvChargeThresholdStore,
    var cache: TtlCache<LenientEvChargeThreshold>,
    var hits: Int,
    var misses: Int,
) : LenientEvChargeThresholdStore {
    override fun find(code: String): LenientEvChargeThreshold? {
        val hit = (cache.get(code) as LenientEvChargeThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientEvChargeThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientEvChargeThreshold> = inner.list()
}

fun newCachedLenientEvChargeThresholdStore(inner: LenientEvChargeThresholdStore, ttl: Int, clock: Clock): LenientEvChargeThresholdStore {
    return CachedLenientEvChargeThresholdStore(
        inner = inner,
        cache = newTtlCache<LenientEvChargeThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient ev charge thresholds. Empty fields match all. */
data class LenientEvChargeThresholdQuery(
    var locale: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientEvChargeThreshold): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientEvChargeThreshold>): MutableList<LenientEvChargeThreshold> {
        val matched = filter(items) { candidate: LenientEvChargeThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientEvChargeThresholds(store: LenientEvChargeThresholdStore, query: LenientEvChargeThresholdQuery): MutableList<LenientEvChargeThreshold> {
    return query.apply(store.list())
}
