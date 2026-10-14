package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy dispute requests by code. */
interface LegacyDisputeRequestStore {
    fun find(code: String): LegacyDisputeRequest?
    fun save(item: LegacyDisputeRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDisputeRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDisputeRequestStore(
    var items: MutableMap<String, LegacyDisputeRequest>,
    var order: MutableList<String>,
) : LegacyDisputeRequestStore {
    override fun find(code: String): LegacyDisputeRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDisputeRequest) {
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

    override fun list(): MutableList<LegacyDisputeRequest> {
        val out = mutableListOf<LegacyDisputeRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDisputeRequestStore(): LegacyDisputeRequestStore {
    return MemoryLegacyDisputeRequestStore(
        mutableMapOf<String, LegacyDisputeRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDisputeRequestStore(
    var inner: LegacyDisputeRequestStore,
    var cache: TtlCache<LegacyDisputeRequest>,
    var hits: Int,
    var misses: Int,
) : LegacyDisputeRequestStore {
    override fun find(code: String): LegacyDisputeRequest? {
        val hit = (cache.get(code) as LegacyDisputeRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDisputeRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDisputeRequest> = inner.list()
}

fun newCachedLegacyDisputeRequestStore(inner: LegacyDisputeRequestStore, ttl: Int, clock: Clock): LegacyDisputeRequestStore {
    return CachedLegacyDisputeRequestStore(
        inner = inner,
        cache = newTtlCache<LegacyDisputeRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy dispute requests. Empty fields match all. */
data class LegacyDisputeRequestQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDisputeRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDisputeRequest>): MutableList<LegacyDisputeRequest> {
        val matched = filter(items) { candidate: LegacyDisputeRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDisputeRequests(store: LegacyDisputeRequestStore, query: LegacyDisputeRequestQuery): MutableList<LegacyDisputeRequest> {
    return query.apply(store.list())
}
