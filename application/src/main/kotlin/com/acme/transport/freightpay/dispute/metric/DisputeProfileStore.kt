package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute profiles by code. */
interface DisputeProfileStore {
    fun find(code: String): DisputeProfile?
    fun save(item: DisputeProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeProfileStore(
    var items: MutableMap<String, DisputeProfile>,
    var order: MutableList<String>,
) : DisputeProfileStore {
    override fun find(code: String): DisputeProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeProfile) {
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

    override fun list(): MutableList<DisputeProfile> {
        val out = mutableListOf<DisputeProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeProfileStore(): DisputeProfileStore {
    return MemoryDisputeProfileStore(mutableMapOf<String, DisputeProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeProfileStore(
    var inner: DisputeProfileStore,
    var cache: TtlCache<DisputeProfile>,
    var hits: Int,
    var misses: Int,
) : DisputeProfileStore {
    override fun find(code: String): DisputeProfile? {
        val hit = (cache.get(code) as DisputeProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeProfile> = inner.list()
}

fun newCachedDisputeProfileStore(inner: DisputeProfileStore, ttl: Int, clock: Clock): DisputeProfileStore {
    return CachedDisputeProfileStore(
        inner = inner,
        cache = newTtlCache<DisputeProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute profiles. Empty fields match all. */
data class DisputeProfileQuery(
    var locale: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeProfile): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeProfile>): MutableList<DisputeProfile> {
        val matched = filter(items) { candidate: DisputeProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeProfiles(store: DisputeProfileStore, query: DisputeProfileQuery): MutableList<DisputeProfile> {
    return query.apply(store.list())
}
