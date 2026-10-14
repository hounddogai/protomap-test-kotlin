package com.acme.transport.lastmile.redelivery.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery digests by code. */
interface RedeliveryDigestStore {
    fun find(code: String): RedeliveryDigest?
    fun save(item: RedeliveryDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryDigestStore(
    var items: MutableMap<String, RedeliveryDigest>,
    var order: MutableList<String>,
) : RedeliveryDigestStore {
    override fun find(code: String): RedeliveryDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryDigest) {
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

    override fun list(): MutableList<RedeliveryDigest> {
        val out = mutableListOf<RedeliveryDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryDigestStore(): RedeliveryDigestStore {
    return MemoryRedeliveryDigestStore(mutableMapOf<String, RedeliveryDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryDigestStore(
    var inner: RedeliveryDigestStore,
    var cache: TtlCache<RedeliveryDigest>,
    var hits: Int,
    var misses: Int,
) : RedeliveryDigestStore {
    override fun find(code: String): RedeliveryDigest? {
        val hit = (cache.get(code) as RedeliveryDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryDigest> = inner.list()
}

fun newCachedRedeliveryDigestStore(inner: RedeliveryDigestStore, ttl: Int, clock: Clock): RedeliveryDigestStore {
    return CachedRedeliveryDigestStore(
        inner = inner,
        cache = newTtlCache<RedeliveryDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery digests. Empty fields match all. */
data class RedeliveryDigestQuery(
    var locale: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryDigest>): MutableList<RedeliveryDigest> {
        val matched = filter(items) { candidate: RedeliveryDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryDigests(store: RedeliveryDigestStore, query: RedeliveryDigestQuery): MutableList<RedeliveryDigest> {
    return query.apply(store.list())
}
