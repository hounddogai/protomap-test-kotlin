package com.acme.transport.freightpay.audit.digest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit digests by code. */
interface AuditDigestStore {
    fun find(code: String): AuditDigest?
    fun save(item: AuditDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditDigestStore(
    var items: MutableMap<String, AuditDigest>,
    var order: MutableList<String>,
) : AuditDigestStore {
    override fun find(code: String): AuditDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditDigest) {
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

    override fun list(): MutableList<AuditDigest> {
        val out = mutableListOf<AuditDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditDigestStore(): AuditDigestStore {
    return MemoryAuditDigestStore(mutableMapOf<String, AuditDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditDigestStore(
    var inner: AuditDigestStore,
    var cache: TtlCache<AuditDigest>,
    var hits: Int,
    var misses: Int,
) : AuditDigestStore {
    override fun find(code: String): AuditDigest? {
        val hit = (cache.get(code) as AuditDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditDigest> = inner.list()
}

fun newCachedAuditDigestStore(inner: AuditDigestStore, ttl: Int, clock: Clock): AuditDigestStore {
    return CachedAuditDigestStore(
        inner = inner,
        cache = newTtlCache<AuditDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit digests. Empty fields match all. */
data class AuditDigestQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditDigest>): MutableList<AuditDigest> {
        val matched = filter(items) { candidate: AuditDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditDigests(store: AuditDigestStore, query: AuditDigestQuery): MutableList<AuditDigest> {
    return query.apply(store.list())
}
