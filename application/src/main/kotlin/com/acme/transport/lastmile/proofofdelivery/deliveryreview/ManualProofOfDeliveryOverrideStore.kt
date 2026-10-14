package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual proof of delivery overrides by code. */
interface ManualProofOfDeliveryOverrideStore {
    fun find(code: String): ManualProofOfDeliveryOverride?
    fun save(item: ManualProofOfDeliveryOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualProofOfDeliveryOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualProofOfDeliveryOverrideStore(
    var items: MutableMap<String, ManualProofOfDeliveryOverride>,
    var order: MutableList<String>,
) : ManualProofOfDeliveryOverrideStore {
    override fun find(code: String): ManualProofOfDeliveryOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualProofOfDeliveryOverride) {
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

    override fun list(): MutableList<ManualProofOfDeliveryOverride> {
        val out = mutableListOf<ManualProofOfDeliveryOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualProofOfDeliveryOverrideStore(): ManualProofOfDeliveryOverrideStore {
    return MemoryManualProofOfDeliveryOverrideStore(
        mutableMapOf<String, ManualProofOfDeliveryOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualProofOfDeliveryOverrideStore(
    var inner: ManualProofOfDeliveryOverrideStore,
    var cache: TtlCache<ManualProofOfDeliveryOverride>,
    var hits: Int,
    var misses: Int,
) : ManualProofOfDeliveryOverrideStore {
    override fun find(code: String): ManualProofOfDeliveryOverride? {
        val hit = (cache.get(code) as ManualProofOfDeliveryOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualProofOfDeliveryOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualProofOfDeliveryOverride> = inner.list()
}

fun newCachedManualProofOfDeliveryOverrideStore(inner: ManualProofOfDeliveryOverrideStore, ttl: Int, clock: Clock): ManualProofOfDeliveryOverrideStore {
    return CachedManualProofOfDeliveryOverrideStore(
        inner = inner,
        cache = newTtlCache<ManualProofOfDeliveryOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual proof of delivery overrides. Empty fields match all. */
data class ManualProofOfDeliveryOverrideQuery(
    var locale: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualProofOfDeliveryOverride): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualProofOfDeliveryOverride>): MutableList<ManualProofOfDeliveryOverride> {
        val matched = filter(items) { candidate: ManualProofOfDeliveryOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualProofOfDeliveryOverrides(store: ManualProofOfDeliveryOverrideStore, query: ManualProofOfDeliveryOverrideQuery): MutableList<ManualProofOfDeliveryOverride> {
    return query.apply(store.list())
}
