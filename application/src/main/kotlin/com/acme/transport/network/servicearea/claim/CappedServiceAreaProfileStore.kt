package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped service area profiles by code. */
interface CappedServiceAreaProfileStore {
    fun find(code: String): CappedServiceAreaProfile?
    fun save(item: CappedServiceAreaProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedServiceAreaProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedServiceAreaProfileStore(
    var items: MutableMap<String, CappedServiceAreaProfile>,
    var order: MutableList<String>,
) : CappedServiceAreaProfileStore {
    override fun find(code: String): CappedServiceAreaProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedServiceAreaProfile) {
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

    override fun list(): MutableList<CappedServiceAreaProfile> {
        val out = mutableListOf<CappedServiceAreaProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedServiceAreaProfileStore(): CappedServiceAreaProfileStore {
    return MemoryCappedServiceAreaProfileStore(
        mutableMapOf<String, CappedServiceAreaProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedServiceAreaProfileStore(
    var inner: CappedServiceAreaProfileStore,
    var cache: TtlCache<CappedServiceAreaProfile>,
    var hits: Int,
    var misses: Int,
) : CappedServiceAreaProfileStore {
    override fun find(code: String): CappedServiceAreaProfile? {
        val hit = (cache.get(code) as CappedServiceAreaProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedServiceAreaProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedServiceAreaProfile> = inner.list()
}

fun newCachedCappedServiceAreaProfileStore(inner: CappedServiceAreaProfileStore, ttl: Int, clock: Clock): CappedServiceAreaProfileStore {
    return CachedCappedServiceAreaProfileStore(
        inner = inner,
        cache = newTtlCache<CappedServiceAreaProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped service area profiles. Empty fields match all. */
data class CappedServiceAreaProfileQuery(
    var channel: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedServiceAreaProfile): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedServiceAreaProfile>): MutableList<CappedServiceAreaProfile> {
        val matched = filter(items) { candidate: CappedServiceAreaProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedServiceAreaProfiles(store: CappedServiceAreaProfileStore, query: CappedServiceAreaProfileQuery): MutableList<CappedServiceAreaProfile> {
    return query.apply(store.list())
}
