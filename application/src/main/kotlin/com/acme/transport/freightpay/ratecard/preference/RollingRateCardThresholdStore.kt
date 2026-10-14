package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling rate card thresholds by code. */
interface RollingRateCardThresholdStore {
    fun find(code: String): RollingRateCardThreshold?
    fun save(item: RollingRateCardThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRateCardThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRateCardThresholdStore(
    var items: MutableMap<String, RollingRateCardThreshold>,
    var order: MutableList<String>,
) : RollingRateCardThresholdStore {
    override fun find(code: String): RollingRateCardThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRateCardThreshold) {
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

    override fun list(): MutableList<RollingRateCardThreshold> {
        val out = mutableListOf<RollingRateCardThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRateCardThresholdStore(): RollingRateCardThresholdStore {
    return MemoryRollingRateCardThresholdStore(
        mutableMapOf<String, RollingRateCardThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRateCardThresholdStore(
    var inner: RollingRateCardThresholdStore,
    var cache: TtlCache<RollingRateCardThreshold>,
    var hits: Int,
    var misses: Int,
) : RollingRateCardThresholdStore {
    override fun find(code: String): RollingRateCardThreshold? {
        val hit = (cache.get(code) as RollingRateCardThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRateCardThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRateCardThreshold> = inner.list()
}

fun newCachedRollingRateCardThresholdStore(inner: RollingRateCardThresholdStore, ttl: Int, clock: Clock): RollingRateCardThresholdStore {
    return CachedRollingRateCardThresholdStore(
        inner = inner,
        cache = newTtlCache<RollingRateCardThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling rate card thresholds. Empty fields match all. */
data class RollingRateCardThresholdQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRateCardThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRateCardThreshold>): MutableList<RollingRateCardThreshold> {
        val matched = filter(items) { candidate: RollingRateCardThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRateCardThresholds(store: RollingRateCardThresholdStore, query: RollingRateCardThresholdQuery): MutableList<RollingRateCardThreshold> {
    return query.apply(store.list())
}
