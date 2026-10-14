package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery profiles by code. */
interface RedeliveryProfileStore {
    fun find(code: String): RedeliveryProfile?
    fun save(item: RedeliveryProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryProfileStore(
    var items: MutableMap<String, RedeliveryProfile>,
    var order: MutableList<String>,
) : RedeliveryProfileStore {
    override fun find(code: String): RedeliveryProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryProfile) {
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

    override fun list(): MutableList<RedeliveryProfile> {
        val out = mutableListOf<RedeliveryProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryProfileStore(): RedeliveryProfileStore {
    return MemoryRedeliveryProfileStore(mutableMapOf<String, RedeliveryProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryProfileStore(
    var inner: RedeliveryProfileStore,
    var cache: TtlCache<RedeliveryProfile>,
    var hits: Int,
    var misses: Int,
) : RedeliveryProfileStore {
    override fun find(code: String): RedeliveryProfile? {
        val hit = (cache.get(code) as RedeliveryProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryProfile> = inner.list()
}

fun newCachedRedeliveryProfileStore(inner: RedeliveryProfileStore, ttl: Int, clock: Clock): RedeliveryProfileStore {
    return CachedRedeliveryProfileStore(
        inner = inner,
        cache = newTtlCache<RedeliveryProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery profiles. Empty fields match all. */
data class RedeliveryProfileQuery(
    var region: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryProfile): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<RedeliveryProfile>): MutableList<RedeliveryProfile> {
        val matched = filter(items) { candidate: RedeliveryProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryProfiles(store: RedeliveryProfileStore, query: RedeliveryProfileQuery): MutableList<RedeliveryProfile> {
    return query.apply(store.list())
}
