package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary ev charge digests by code. */
interface PrimaryEvChargeDigestStore {
    fun find(code: String): PrimaryEvChargeDigest?
    fun save(item: PrimaryEvChargeDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryEvChargeDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryEvChargeDigestStore(
    var items: MutableMap<String, PrimaryEvChargeDigest>,
    var order: MutableList<String>,
) : PrimaryEvChargeDigestStore {
    override fun find(code: String): PrimaryEvChargeDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryEvChargeDigest) {
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

    override fun list(): MutableList<PrimaryEvChargeDigest> {
        val out = mutableListOf<PrimaryEvChargeDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryEvChargeDigestStore(): PrimaryEvChargeDigestStore {
    return MemoryPrimaryEvChargeDigestStore(
        mutableMapOf<String, PrimaryEvChargeDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryEvChargeDigestStore(
    var inner: PrimaryEvChargeDigestStore,
    var cache: TtlCache<PrimaryEvChargeDigest>,
    var hits: Int,
    var misses: Int,
) : PrimaryEvChargeDigestStore {
    override fun find(code: String): PrimaryEvChargeDigest? {
        val hit = (cache.get(code) as PrimaryEvChargeDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryEvChargeDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryEvChargeDigest> = inner.list()
}

fun newCachedPrimaryEvChargeDigestStore(inner: PrimaryEvChargeDigestStore, ttl: Int, clock: Clock): PrimaryEvChargeDigestStore {
    return CachedPrimaryEvChargeDigestStore(
        inner = inner,
        cache = newTtlCache<PrimaryEvChargeDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary ev charge digests. Empty fields match all. */
data class PrimaryEvChargeDigestQuery(
    var region: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryEvChargeDigest): Boolean {
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

    fun apply(items: MutableList<PrimaryEvChargeDigest>): MutableList<PrimaryEvChargeDigest> {
        val matched = filter(items) { candidate: PrimaryEvChargeDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryEvChargeDigests(store: PrimaryEvChargeDigestStore, query: PrimaryEvChargeDigestQuery): MutableList<PrimaryEvChargeDigest> {
    return query.apply(store.list())
}
