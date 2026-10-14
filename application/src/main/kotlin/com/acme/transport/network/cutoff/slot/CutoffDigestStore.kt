package com.acme.transport.network.cutoff.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff digests by code. */
interface CutoffDigestStore {
    fun find(code: String): CutoffDigest?
    fun save(item: CutoffDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffDigestStore(
    var items: MutableMap<String, CutoffDigest>,
    var order: MutableList<String>,
) : CutoffDigestStore {
    override fun find(code: String): CutoffDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffDigest) {
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

    override fun list(): MutableList<CutoffDigest> {
        val out = mutableListOf<CutoffDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffDigestStore(): CutoffDigestStore {
    return MemoryCutoffDigestStore(mutableMapOf<String, CutoffDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffDigestStore(
    var inner: CutoffDigestStore,
    var cache: TtlCache<CutoffDigest>,
    var hits: Int,
    var misses: Int,
) : CutoffDigestStore {
    override fun find(code: String): CutoffDigest? {
        val hit = (cache.get(code) as CutoffDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffDigest> = inner.list()
}

fun newCachedCutoffDigestStore(inner: CutoffDigestStore, ttl: Int, clock: Clock): CutoffDigestStore {
    return CachedCutoffDigestStore(
        inner = inner,
        cache = newTtlCache<CutoffDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff digests. Empty fields match all. */
data class CutoffDigestQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffDigest>): MutableList<CutoffDigest> {
        val matched = filter(items) { candidate: CutoffDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffDigests(store: CutoffDigestStore, query: CutoffDigestQuery): MutableList<CutoffDigest> {
    return query.apply(store.list())
}
