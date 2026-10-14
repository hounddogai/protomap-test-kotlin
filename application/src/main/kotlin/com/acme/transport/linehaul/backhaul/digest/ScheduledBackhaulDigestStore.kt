package com.acme.transport.linehaul.backhaul.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled backhaul digests by code. */
interface ScheduledBackhaulDigestStore {
    fun find(code: String): ScheduledBackhaulDigest?
    fun save(item: ScheduledBackhaulDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledBackhaulDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledBackhaulDigestStore(
    var items: MutableMap<String, ScheduledBackhaulDigest>,
    var order: MutableList<String>,
) : ScheduledBackhaulDigestStore {
    override fun find(code: String): ScheduledBackhaulDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledBackhaulDigest) {
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

    override fun list(): MutableList<ScheduledBackhaulDigest> {
        val out = mutableListOf<ScheduledBackhaulDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledBackhaulDigestStore(): ScheduledBackhaulDigestStore {
    return MemoryScheduledBackhaulDigestStore(
        mutableMapOf<String, ScheduledBackhaulDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledBackhaulDigestStore(
    var inner: ScheduledBackhaulDigestStore,
    var cache: TtlCache<ScheduledBackhaulDigest>,
    var hits: Int,
    var misses: Int,
) : ScheduledBackhaulDigestStore {
    override fun find(code: String): ScheduledBackhaulDigest? {
        val hit = (cache.get(code) as ScheduledBackhaulDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledBackhaulDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledBackhaulDigest> = inner.list()
}

fun newCachedScheduledBackhaulDigestStore(inner: ScheduledBackhaulDigestStore, ttl: Int, clock: Clock): ScheduledBackhaulDigestStore {
    return CachedScheduledBackhaulDigestStore(
        inner = inner,
        cache = newTtlCache<ScheduledBackhaulDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled backhaul digests. Empty fields match all. */
data class ScheduledBackhaulDigestQuery(
    var locale: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledBackhaulDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledBackhaulDigest>): MutableList<ScheduledBackhaulDigest> {
        val matched = filter(items) { candidate: ScheduledBackhaulDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledBackhaulDigests(store: ScheduledBackhaulDigestStore, query: ScheduledBackhaulDigestQuery): MutableList<ScheduledBackhaulDigest> {
    return query.apply(store.list())
}
