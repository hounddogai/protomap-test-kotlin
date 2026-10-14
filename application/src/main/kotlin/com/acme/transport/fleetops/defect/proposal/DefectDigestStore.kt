package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect digests by code. */
interface DefectDigestStore {
    fun find(code: String): DefectDigest?
    fun save(item: DefectDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectDigestStore(
    var items: MutableMap<String, DefectDigest>,
    var order: MutableList<String>,
) : DefectDigestStore {
    override fun find(code: String): DefectDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectDigest) {
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

    override fun list(): MutableList<DefectDigest> {
        val out = mutableListOf<DefectDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectDigestStore(): DefectDigestStore {
    return MemoryDefectDigestStore(mutableMapOf<String, DefectDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectDigestStore(
    var inner: DefectDigestStore,
    var cache: TtlCache<DefectDigest>,
    var hits: Int,
    var misses: Int,
) : DefectDigestStore {
    override fun find(code: String): DefectDigest? {
        val hit = (cache.get(code) as DefectDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectDigest> = inner.list()
}

fun newCachedDefectDigestStore(inner: DefectDigestStore, ttl: Int, clock: Clock): DefectDigestStore {
    return CachedDefectDigestStore(
        inner = inner,
        cache = newTtlCache<DefectDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect digests. Empty fields match all. */
data class DefectDigestQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectDigest>): MutableList<DefectDigest> {
        val matched = filter(items) { candidate: DefectDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectDigests(store: DefectDigestStore, query: DefectDigestQuery): MutableList<DefectDigest> {
    return query.apply(store.list())
}
