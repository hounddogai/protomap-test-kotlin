package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict dwell digests by code. */
interface StrictDwellDigestStore {
    fun find(code: String): StrictDwellDigest?
    fun save(item: StrictDwellDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDwellDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDwellDigestStore(
    var items: MutableMap<String, StrictDwellDigest>,
    var order: MutableList<String>,
) : StrictDwellDigestStore {
    override fun find(code: String): StrictDwellDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDwellDigest) {
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

    override fun list(): MutableList<StrictDwellDigest> {
        val out = mutableListOf<StrictDwellDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDwellDigestStore(): StrictDwellDigestStore {
    return MemoryStrictDwellDigestStore(mutableMapOf<String, StrictDwellDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDwellDigestStore(
    var inner: StrictDwellDigestStore,
    var cache: TtlCache<StrictDwellDigest>,
    var hits: Int,
    var misses: Int,
) : StrictDwellDigestStore {
    override fun find(code: String): StrictDwellDigest? {
        val hit = (cache.get(code) as StrictDwellDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDwellDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDwellDigest> = inner.list()
}

fun newCachedStrictDwellDigestStore(inner: StrictDwellDigestStore, ttl: Int, clock: Clock): StrictDwellDigestStore {
    return CachedStrictDwellDigestStore(
        inner = inner,
        cache = newTtlCache<StrictDwellDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dwell digests. Empty fields match all. */
data class StrictDwellDigestQuery(
    var unit: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDwellDigest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<StrictDwellDigest>): MutableList<StrictDwellDigest> {
        val matched = filter(items) { candidate: StrictDwellDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDwellDigests(store: StrictDwellDigestStore, query: StrictDwellDigestQuery): MutableList<StrictDwellDigest> {
    return query.apply(store.list())
}
