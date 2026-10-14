package com.acme.transport.terminal.sortation.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered sortation digests by code. */
interface TieredSortationDigestStore {
    fun find(code: String): TieredSortationDigest?
    fun save(item: TieredSortationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredSortationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredSortationDigestStore(
    var items: MutableMap<String, TieredSortationDigest>,
    var order: MutableList<String>,
) : TieredSortationDigestStore {
    override fun find(code: String): TieredSortationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredSortationDigest) {
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

    override fun list(): MutableList<TieredSortationDigest> {
        val out = mutableListOf<TieredSortationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredSortationDigestStore(): TieredSortationDigestStore {
    return MemoryTieredSortationDigestStore(
        mutableMapOf<String, TieredSortationDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredSortationDigestStore(
    var inner: TieredSortationDigestStore,
    var cache: TtlCache<TieredSortationDigest>,
    var hits: Int,
    var misses: Int,
) : TieredSortationDigestStore {
    override fun find(code: String): TieredSortationDigest? {
        val hit = (cache.get(code) as TieredSortationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredSortationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredSortationDigest> = inner.list()
}

fun newCachedTieredSortationDigestStore(inner: TieredSortationDigestStore, ttl: Int, clock: Clock): TieredSortationDigestStore {
    return CachedTieredSortationDigestStore(
        inner = inner,
        cache = newTtlCache<TieredSortationDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered sortation digests. Empty fields match all. */
data class TieredSortationDigestQuery(
    var segment: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredSortationDigest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredSortationDigest>): MutableList<TieredSortationDigest> {
        val matched = filter(items) { candidate: TieredSortationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredSortationDigests(store: TieredSortationDigestStore, query: TieredSortationDigestQuery): MutableList<TieredSortationDigest> {
    return query.apply(store.list())
}
