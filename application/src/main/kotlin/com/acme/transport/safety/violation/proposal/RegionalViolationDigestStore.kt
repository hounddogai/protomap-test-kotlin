package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional violation digests by code. */
interface RegionalViolationDigestStore {
    fun find(code: String): RegionalViolationDigest?
    fun save(item: RegionalViolationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalViolationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalViolationDigestStore(
    var items: MutableMap<String, RegionalViolationDigest>,
    var order: MutableList<String>,
) : RegionalViolationDigestStore {
    override fun find(code: String): RegionalViolationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalViolationDigest) {
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

    override fun list(): MutableList<RegionalViolationDigest> {
        val out = mutableListOf<RegionalViolationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalViolationDigestStore(): RegionalViolationDigestStore {
    return MemoryRegionalViolationDigestStore(
        mutableMapOf<String, RegionalViolationDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalViolationDigestStore(
    var inner: RegionalViolationDigestStore,
    var cache: TtlCache<RegionalViolationDigest>,
    var hits: Int,
    var misses: Int,
) : RegionalViolationDigestStore {
    override fun find(code: String): RegionalViolationDigest? {
        val hit = (cache.get(code) as RegionalViolationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalViolationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalViolationDigest> = inner.list()
}

fun newCachedRegionalViolationDigestStore(inner: RegionalViolationDigestStore, ttl: Int, clock: Clock): RegionalViolationDigestStore {
    return CachedRegionalViolationDigestStore(
        inner = inner,
        cache = newTtlCache<RegionalViolationDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional violation digests. Empty fields match all. */
data class RegionalViolationDigestQuery(
    var segment: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalViolationDigest): Boolean {
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

    fun apply(items: MutableList<RegionalViolationDigest>): MutableList<RegionalViolationDigest> {
        val matched = filter(items) { candidate: RegionalViolationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalViolationDigests(store: RegionalViolationDigestStore, query: RegionalViolationDigestQuery): MutableList<RegionalViolationDigest> {
    return query.apply(store.list())
}
