package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute claims by code. */
interface DisputeClaimStore {
    fun find(code: String): DisputeClaim?
    fun save(item: DisputeClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeClaimStore(
    var items: MutableMap<String, DisputeClaim>,
    var order: MutableList<String>,
) : DisputeClaimStore {
    override fun find(code: String): DisputeClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeClaim) {
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

    override fun list(): MutableList<DisputeClaim> {
        val out = mutableListOf<DisputeClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeClaimStore(): DisputeClaimStore {
    return MemoryDisputeClaimStore(mutableMapOf<String, DisputeClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeClaimStore(
    var inner: DisputeClaimStore,
    var cache: TtlCache<DisputeClaim>,
    var hits: Int,
    var misses: Int,
) : DisputeClaimStore {
    override fun find(code: String): DisputeClaim? {
        val hit = (cache.get(code) as DisputeClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeClaim> = inner.list()
}

fun newCachedDisputeClaimStore(inner: DisputeClaimStore, ttl: Int, clock: Clock): DisputeClaimStore {
    return CachedDisputeClaimStore(
        inner = inner,
        cache = newTtlCache<DisputeClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute claims. Empty fields match all. */
data class DisputeClaimQuery(
    var source: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeClaim): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<DisputeClaim>): MutableList<DisputeClaim> {
        val matched = filter(items) { candidate: DisputeClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeClaims(store: DisputeClaimStore, query: DisputeClaimQuery): MutableList<DisputeClaim> {
    return query.apply(store.list())
}
