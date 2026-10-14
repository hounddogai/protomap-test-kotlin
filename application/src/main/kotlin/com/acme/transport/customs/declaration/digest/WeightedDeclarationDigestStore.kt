package com.acme.transport.customs.declaration.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted declaration digests by code. */
interface WeightedDeclarationDigestStore {
    fun find(code: String): WeightedDeclarationDigest?
    fun save(item: WeightedDeclarationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDeclarationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDeclarationDigestStore(
    var items: MutableMap<String, WeightedDeclarationDigest>,
    var order: MutableList<String>,
) : WeightedDeclarationDigestStore {
    override fun find(code: String): WeightedDeclarationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDeclarationDigest) {
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

    override fun list(): MutableList<WeightedDeclarationDigest> {
        val out = mutableListOf<WeightedDeclarationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDeclarationDigestStore(): WeightedDeclarationDigestStore {
    return MemoryWeightedDeclarationDigestStore(
        mutableMapOf<String, WeightedDeclarationDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDeclarationDigestStore(
    var inner: WeightedDeclarationDigestStore,
    var cache: TtlCache<WeightedDeclarationDigest>,
    var hits: Int,
    var misses: Int,
) : WeightedDeclarationDigestStore {
    override fun find(code: String): WeightedDeclarationDigest? {
        val hit = (cache.get(code) as WeightedDeclarationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDeclarationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDeclarationDigest> = inner.list()
}

fun newCachedWeightedDeclarationDigestStore(inner: WeightedDeclarationDigestStore, ttl: Int, clock: Clock): WeightedDeclarationDigestStore {
    return CachedWeightedDeclarationDigestStore(
        inner = inner,
        cache = newTtlCache<WeightedDeclarationDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted declaration digests. Empty fields match all. */
data class WeightedDeclarationDigestQuery(
    var country: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDeclarationDigest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDeclarationDigest>): MutableList<WeightedDeclarationDigest> {
        val matched = filter(items) { candidate: WeightedDeclarationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDeclarationDigests(store: WeightedDeclarationDigestStore, query: WeightedDeclarationDigestQuery): MutableList<WeightedDeclarationDigest> {
    return query.apply(store.list())
}
