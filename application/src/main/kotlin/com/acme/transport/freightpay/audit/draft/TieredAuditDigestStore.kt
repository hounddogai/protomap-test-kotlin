package com.acme.transport.freightpay.audit.draft

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered audit digests by code. */
interface TieredAuditDigestStore {
    fun find(code: String): TieredAuditDigest?
    fun save(item: TieredAuditDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredAuditDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredAuditDigestStore(
    var items: MutableMap<String, TieredAuditDigest>,
    var order: MutableList<String>,
) : TieredAuditDigestStore {
    override fun find(code: String): TieredAuditDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredAuditDigest) {
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

    override fun list(): MutableList<TieredAuditDigest> {
        val out = mutableListOf<TieredAuditDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredAuditDigestStore(): TieredAuditDigestStore {
    return MemoryTieredAuditDigestStore(mutableMapOf<String, TieredAuditDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredAuditDigestStore(
    var inner: TieredAuditDigestStore,
    var cache: TtlCache<TieredAuditDigest>,
    var hits: Int,
    var misses: Int,
) : TieredAuditDigestStore {
    override fun find(code: String): TieredAuditDigest? {
        val hit = (cache.get(code) as TieredAuditDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredAuditDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredAuditDigest> = inner.list()
}

fun newCachedTieredAuditDigestStore(inner: TieredAuditDigestStore, ttl: Int, clock: Clock): TieredAuditDigestStore {
    return CachedTieredAuditDigestStore(
        inner = inner,
        cache = newTtlCache<TieredAuditDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered audit digests. Empty fields match all. */
data class TieredAuditDigestQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredAuditDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TieredAuditDigest>): MutableList<TieredAuditDigest> {
        val matched = filter(items) { candidate: TieredAuditDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredAuditDigests(store: TieredAuditDigestStore, query: TieredAuditDigestQuery): MutableList<TieredAuditDigest> {
    return query.apply(store.list())
}
