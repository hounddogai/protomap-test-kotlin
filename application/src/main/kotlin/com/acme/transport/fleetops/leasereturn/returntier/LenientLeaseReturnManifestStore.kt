package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient lease return manifests by code. */
interface LenientLeaseReturnManifestStore {
    fun find(code: String): LenientLeaseReturnManifest?
    fun save(item: LenientLeaseReturnManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientLeaseReturnManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientLeaseReturnManifestStore(
    var items: MutableMap<String, LenientLeaseReturnManifest>,
    var order: MutableList<String>,
) : LenientLeaseReturnManifestStore {
    override fun find(code: String): LenientLeaseReturnManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientLeaseReturnManifest) {
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

    override fun list(): MutableList<LenientLeaseReturnManifest> {
        val out = mutableListOf<LenientLeaseReturnManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientLeaseReturnManifestStore(): LenientLeaseReturnManifestStore {
    return MemoryLenientLeaseReturnManifestStore(
        mutableMapOf<String, LenientLeaseReturnManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientLeaseReturnManifestStore(
    var inner: LenientLeaseReturnManifestStore,
    var cache: TtlCache<LenientLeaseReturnManifest>,
    var hits: Int,
    var misses: Int,
) : LenientLeaseReturnManifestStore {
    override fun find(code: String): LenientLeaseReturnManifest? {
        val hit = (cache.get(code) as LenientLeaseReturnManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientLeaseReturnManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientLeaseReturnManifest> = inner.list()
}

fun newCachedLenientLeaseReturnManifestStore(inner: LenientLeaseReturnManifestStore, ttl: Int, clock: Clock): LenientLeaseReturnManifestStore {
    return CachedLenientLeaseReturnManifestStore(
        inner = inner,
        cache = newTtlCache<LenientLeaseReturnManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient lease return manifests. Empty fields match all. */
data class LenientLeaseReturnManifestQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientLeaseReturnManifest): Boolean {
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

    fun apply(items: MutableList<LenientLeaseReturnManifest>): MutableList<LenientLeaseReturnManifest> {
        val matched = filter(items) { candidate: LenientLeaseReturnManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientLeaseReturnManifests(store: LenientLeaseReturnManifestStore, query: LenientLeaseReturnManifestQuery): MutableList<LenientLeaseReturnManifest> {
    return query.apply(store.list())
}
