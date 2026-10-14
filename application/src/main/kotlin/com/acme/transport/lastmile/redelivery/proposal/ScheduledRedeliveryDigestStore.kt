package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled redelivery digests by code. */
interface ScheduledRedeliveryDigestStore {
    fun find(code: String): ScheduledRedeliveryDigest?
    fun save(item: ScheduledRedeliveryDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRedeliveryDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRedeliveryDigestStore(
    var items: MutableMap<String, ScheduledRedeliveryDigest>,
    var order: MutableList<String>,
) : ScheduledRedeliveryDigestStore {
    override fun find(code: String): ScheduledRedeliveryDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRedeliveryDigest) {
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

    override fun list(): MutableList<ScheduledRedeliveryDigest> {
        val out = mutableListOf<ScheduledRedeliveryDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRedeliveryDigestStore(): ScheduledRedeliveryDigestStore {
    return MemoryScheduledRedeliveryDigestStore(
        mutableMapOf<String, ScheduledRedeliveryDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRedeliveryDigestStore(
    var inner: ScheduledRedeliveryDigestStore,
    var cache: TtlCache<ScheduledRedeliveryDigest>,
    var hits: Int,
    var misses: Int,
) : ScheduledRedeliveryDigestStore {
    override fun find(code: String): ScheduledRedeliveryDigest? {
        val hit = (cache.get(code) as ScheduledRedeliveryDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRedeliveryDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRedeliveryDigest> = inner.list()
}

fun newCachedScheduledRedeliveryDigestStore(inner: ScheduledRedeliveryDigestStore, ttl: Int, clock: Clock): ScheduledRedeliveryDigestStore {
    return CachedScheduledRedeliveryDigestStore(
        inner = inner,
        cache = newTtlCache<ScheduledRedeliveryDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled redelivery digests. Empty fields match all. */
data class ScheduledRedeliveryDigestQuery(
    var locale: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRedeliveryDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRedeliveryDigest>): MutableList<ScheduledRedeliveryDigest> {
        val matched = filter(items) { candidate: ScheduledRedeliveryDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRedeliveryDigests(store: ScheduledRedeliveryDigestStore, query: ScheduledRedeliveryDigestQuery): MutableList<ScheduledRedeliveryDigest> {
    return query.apply(store.list())
}
