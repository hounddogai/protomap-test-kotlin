package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling proof of delivery thresholds by code. */
interface RollingProofOfDeliveryThresholdStore {
    fun find(code: String): RollingProofOfDeliveryThreshold?
    fun save(item: RollingProofOfDeliveryThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingProofOfDeliveryThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingProofOfDeliveryThresholdStore(
    var items: MutableMap<String, RollingProofOfDeliveryThreshold>,
    var order: MutableList<String>,
) : RollingProofOfDeliveryThresholdStore {
    override fun find(code: String): RollingProofOfDeliveryThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingProofOfDeliveryThreshold) {
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

    override fun list(): MutableList<RollingProofOfDeliveryThreshold> {
        val out = mutableListOf<RollingProofOfDeliveryThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingProofOfDeliveryThresholdStore(): RollingProofOfDeliveryThresholdStore {
    return MemoryRollingProofOfDeliveryThresholdStore(
        mutableMapOf<String, RollingProofOfDeliveryThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingProofOfDeliveryThresholdStore(
    var inner: RollingProofOfDeliveryThresholdStore,
    var cache: TtlCache<RollingProofOfDeliveryThreshold>,
    var hits: Int,
    var misses: Int,
) : RollingProofOfDeliveryThresholdStore {
    override fun find(code: String): RollingProofOfDeliveryThreshold? {
        val hit = (cache.get(code) as RollingProofOfDeliveryThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingProofOfDeliveryThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingProofOfDeliveryThreshold> = inner.list()
}

fun newCachedRollingProofOfDeliveryThresholdStore(inner: RollingProofOfDeliveryThresholdStore, ttl: Int, clock: Clock): RollingProofOfDeliveryThresholdStore {
    return CachedRollingProofOfDeliveryThresholdStore(
        inner = inner,
        cache = newTtlCache<RollingProofOfDeliveryThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling proof of delivery thresholds. Empty fields match all. */
data class RollingProofOfDeliveryThresholdQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingProofOfDeliveryThreshold): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingProofOfDeliveryThreshold>): MutableList<RollingProofOfDeliveryThreshold> {
        val matched = filter(items) { candidate: RollingProofOfDeliveryThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingProofOfDeliveryThresholds(store: RollingProofOfDeliveryThresholdStore, query: RollingProofOfDeliveryThresholdQuery): MutableList<RollingProofOfDeliveryThreshold> {
    return query.apply(store.list())
}
