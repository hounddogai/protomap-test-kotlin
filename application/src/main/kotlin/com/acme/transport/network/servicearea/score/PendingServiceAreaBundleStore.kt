package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending service area bundles by code. */
interface PendingServiceAreaBundleStore {
    fun find(code: String): PendingServiceAreaBundle?
    fun save(item: PendingServiceAreaBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingServiceAreaBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingServiceAreaBundleStore(
    var items: MutableMap<String, PendingServiceAreaBundle>,
    var order: MutableList<String>,
) : PendingServiceAreaBundleStore {
    override fun find(code: String): PendingServiceAreaBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingServiceAreaBundle) {
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

    override fun list(): MutableList<PendingServiceAreaBundle> {
        val out = mutableListOf<PendingServiceAreaBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingServiceAreaBundleStore(): PendingServiceAreaBundleStore {
    return MemoryPendingServiceAreaBundleStore(
        mutableMapOf<String, PendingServiceAreaBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingServiceAreaBundleStore(
    var inner: PendingServiceAreaBundleStore,
    var cache: TtlCache<PendingServiceAreaBundle>,
    var hits: Int,
    var misses: Int,
) : PendingServiceAreaBundleStore {
    override fun find(code: String): PendingServiceAreaBundle? {
        val hit = (cache.get(code) as PendingServiceAreaBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingServiceAreaBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingServiceAreaBundle> = inner.list()
}

fun newCachedPendingServiceAreaBundleStore(inner: PendingServiceAreaBundleStore, ttl: Int, clock: Clock): PendingServiceAreaBundleStore {
    return CachedPendingServiceAreaBundleStore(
        inner = inner,
        cache = newTtlCache<PendingServiceAreaBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending service area bundles. Empty fields match all. */
data class PendingServiceAreaBundleQuery(
    var region: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingServiceAreaBundle): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingServiceAreaBundle>): MutableList<PendingServiceAreaBundle> {
        val matched = filter(items) { candidate: PendingServiceAreaBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingServiceAreaBundles(store: PendingServiceAreaBundleStore, query: PendingServiceAreaBundleQuery): MutableList<PendingServiceAreaBundle> {
    return query.apply(store.list())
}
