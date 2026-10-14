package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending proof of delivery baselines by code. */
interface PendingProofOfDeliveryBaselineStore {
    fun find(code: String): PendingProofOfDeliveryBaseline?
    fun save(item: PendingProofOfDeliveryBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingProofOfDeliveryBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingProofOfDeliveryBaselineStore(
    var items: MutableMap<String, PendingProofOfDeliveryBaseline>,
    var order: MutableList<String>,
) : PendingProofOfDeliveryBaselineStore {
    override fun find(code: String): PendingProofOfDeliveryBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingProofOfDeliveryBaseline) {
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

    override fun list(): MutableList<PendingProofOfDeliveryBaseline> {
        val out = mutableListOf<PendingProofOfDeliveryBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingProofOfDeliveryBaselineStore(): PendingProofOfDeliveryBaselineStore {
    return MemoryPendingProofOfDeliveryBaselineStore(
        mutableMapOf<String, PendingProofOfDeliveryBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingProofOfDeliveryBaselineStore(
    var inner: PendingProofOfDeliveryBaselineStore,
    var cache: TtlCache<PendingProofOfDeliveryBaseline>,
    var hits: Int,
    var misses: Int,
) : PendingProofOfDeliveryBaselineStore {
    override fun find(code: String): PendingProofOfDeliveryBaseline? {
        val hit = (cache.get(code) as PendingProofOfDeliveryBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingProofOfDeliveryBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingProofOfDeliveryBaseline> = inner.list()
}

fun newCachedPendingProofOfDeliveryBaselineStore(inner: PendingProofOfDeliveryBaselineStore, ttl: Int, clock: Clock): PendingProofOfDeliveryBaselineStore {
    return CachedPendingProofOfDeliveryBaselineStore(
        inner = inner,
        cache = newTtlCache<PendingProofOfDeliveryBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending proof of delivery baselines. Empty fields match all. */
data class PendingProofOfDeliveryBaselineQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingProofOfDeliveryBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingProofOfDeliveryBaseline>): MutableList<PendingProofOfDeliveryBaseline> {
        val matched = filter(items) { candidate: PendingProofOfDeliveryBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingProofOfDeliveryBaselines(store: PendingProofOfDeliveryBaselineStore, query: PendingProofOfDeliveryBaselineQuery): MutableList<PendingProofOfDeliveryBaseline> {
    return query.apply(store.list())
}
