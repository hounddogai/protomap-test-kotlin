package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity digests by code. */
interface CapacityDigestStore {
    fun find(code: String): CapacityDigest?
    fun save(item: CapacityDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityDigestStore(
    var items: MutableMap<String, CapacityDigest>,
    var order: MutableList<String>,
) : CapacityDigestStore {
    override fun find(code: String): CapacityDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityDigest) {
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

    override fun list(): MutableList<CapacityDigest> {
        val out = mutableListOf<CapacityDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityDigestStore(): CapacityDigestStore {
    return MemoryCapacityDigestStore(mutableMapOf<String, CapacityDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityDigestStore(
    var inner: CapacityDigestStore,
    var cache: TtlCache<CapacityDigest>,
    var hits: Int,
    var misses: Int,
) : CapacityDigestStore {
    override fun find(code: String): CapacityDigest? {
        val hit = (cache.get(code) as CapacityDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityDigest> = inner.list()
}

fun newCachedCapacityDigestStore(inner: CapacityDigestStore, ttl: Int, clock: Clock): CapacityDigestStore {
    return CachedCapacityDigestStore(
        inner = inner,
        cache = newTtlCache<CapacityDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity digests. Empty fields match all. */
data class CapacityDigestQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityDigest>): MutableList<CapacityDigest> {
        val matched = filter(items) { candidate: CapacityDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityDigests(store: CapacityDigestStore, query: CapacityDigestQuery): MutableList<CapacityDigest> {
    return query.apply(store.list())
}
