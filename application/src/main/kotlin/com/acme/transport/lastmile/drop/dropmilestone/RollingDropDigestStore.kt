package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling drop digests by code. */
interface RollingDropDigestStore {
    fun find(code: String): RollingDropDigest?
    fun save(item: RollingDropDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDropDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDropDigestStore(
    var items: MutableMap<String, RollingDropDigest>,
    var order: MutableList<String>,
) : RollingDropDigestStore {
    override fun find(code: String): RollingDropDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDropDigest) {
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

    override fun list(): MutableList<RollingDropDigest> {
        val out = mutableListOf<RollingDropDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDropDigestStore(): RollingDropDigestStore {
    return MemoryRollingDropDigestStore(mutableMapOf<String, RollingDropDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDropDigestStore(
    var inner: RollingDropDigestStore,
    var cache: TtlCache<RollingDropDigest>,
    var hits: Int,
    var misses: Int,
) : RollingDropDigestStore {
    override fun find(code: String): RollingDropDigest? {
        val hit = (cache.get(code) as RollingDropDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDropDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDropDigest> = inner.list()
}

fun newCachedRollingDropDigestStore(inner: RollingDropDigestStore, ttl: Int, clock: Clock): RollingDropDigestStore {
    return CachedRollingDropDigestStore(
        inner = inner,
        cache = newTtlCache<RollingDropDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling drop digests. Empty fields match all. */
data class RollingDropDigestQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDropDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDropDigest>): MutableList<RollingDropDigest> {
        val matched = filter(items) { candidate: RollingDropDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDropDigests(store: RollingDropDigestStore, query: RollingDropDigestQuery): MutableList<RollingDropDigest> {
    return query.apply(store.list())
}
