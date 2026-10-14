package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute digests by code. */
interface DisputeDigestStore {
    fun find(code: String): DisputeDigest?
    fun save(item: DisputeDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeDigestStore(
    var items: MutableMap<String, DisputeDigest>,
    var order: MutableList<String>,
) : DisputeDigestStore {
    override fun find(code: String): DisputeDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeDigest) {
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

    override fun list(): MutableList<DisputeDigest> {
        val out = mutableListOf<DisputeDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeDigestStore(): DisputeDigestStore {
    return MemoryDisputeDigestStore(mutableMapOf<String, DisputeDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeDigestStore(
    var inner: DisputeDigestStore,
    var cache: TtlCache<DisputeDigest>,
    var hits: Int,
    var misses: Int,
) : DisputeDigestStore {
    override fun find(code: String): DisputeDigest? {
        val hit = (cache.get(code) as DisputeDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeDigest> = inner.list()
}

fun newCachedDisputeDigestStore(inner: DisputeDigestStore, ttl: Int, clock: Clock): DisputeDigestStore {
    return CachedDisputeDigestStore(
        inner = inner,
        cache = newTtlCache<DisputeDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute digests. Empty fields match all. */
data class DisputeDigestQuery(
    var category: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeDigest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeDigest>): MutableList<DisputeDigest> {
        val matched = filter(items) { candidate: DisputeDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeDigests(store: DisputeDigestStore, query: DisputeDigestQuery): MutableList<DisputeDigest> {
    return query.apply(store.list())
}
