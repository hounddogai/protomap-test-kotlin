package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending sortation digests by code. */
interface PendingSortationDigestStore {
    fun find(code: String): PendingSortationDigest?
    fun save(item: PendingSortationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingSortationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingSortationDigestStore(
    var items: MutableMap<String, PendingSortationDigest>,
    var order: MutableList<String>,
) : PendingSortationDigestStore {
    override fun find(code: String): PendingSortationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingSortationDigest) {
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

    override fun list(): MutableList<PendingSortationDigest> {
        val out = mutableListOf<PendingSortationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingSortationDigestStore(): PendingSortationDigestStore {
    return MemoryPendingSortationDigestStore(
        mutableMapOf<String, PendingSortationDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingSortationDigestStore(
    var inner: PendingSortationDigestStore,
    var cache: TtlCache<PendingSortationDigest>,
    var hits: Int,
    var misses: Int,
) : PendingSortationDigestStore {
    override fun find(code: String): PendingSortationDigest? {
        val hit = (cache.get(code) as PendingSortationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingSortationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingSortationDigest> = inner.list()
}

fun newCachedPendingSortationDigestStore(inner: PendingSortationDigestStore, ttl: Int, clock: Clock): PendingSortationDigestStore {
    return CachedPendingSortationDigestStore(
        inner = inner,
        cache = newTtlCache<PendingSortationDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending sortation digests. Empty fields match all. */
data class PendingSortationDigestQuery(
    var unit: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingSortationDigest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingSortationDigest>): MutableList<PendingSortationDigest> {
        val matched = filter(items) { candidate: PendingSortationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingSortationDigests(store: PendingSortationDigestStore, query: PendingSortationDigestQuery): MutableList<PendingSortationDigest> {
    return query.apply(store.list())
}
