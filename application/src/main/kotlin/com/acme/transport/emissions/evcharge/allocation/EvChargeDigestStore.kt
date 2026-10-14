package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge digests by code. */
interface EvChargeDigestStore {
    fun find(code: String): EvChargeDigest?
    fun save(item: EvChargeDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeDigestStore(
    var items: MutableMap<String, EvChargeDigest>,
    var order: MutableList<String>,
) : EvChargeDigestStore {
    override fun find(code: String): EvChargeDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeDigest) {
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

    override fun list(): MutableList<EvChargeDigest> {
        val out = mutableListOf<EvChargeDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeDigestStore(): EvChargeDigestStore {
    return MemoryEvChargeDigestStore(mutableMapOf<String, EvChargeDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeDigestStore(
    var inner: EvChargeDigestStore,
    var cache: TtlCache<EvChargeDigest>,
    var hits: Int,
    var misses: Int,
) : EvChargeDigestStore {
    override fun find(code: String): EvChargeDigest? {
        val hit = (cache.get(code) as EvChargeDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeDigest> = inner.list()
}

fun newCachedEvChargeDigestStore(inner: EvChargeDigestStore, ttl: Int, clock: Clock): EvChargeDigestStore {
    return CachedEvChargeDigestStore(
        inner = inner,
        cache = newTtlCache<EvChargeDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge digests. Empty fields match all. */
data class EvChargeDigestQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeDigest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<EvChargeDigest>): MutableList<EvChargeDigest> {
        val matched = filter(items) { candidate: EvChargeDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeDigests(store: EvChargeDigestStore, query: EvChargeDigestQuery): MutableList<EvChargeDigest> {
    return query.apply(store.list())
}
