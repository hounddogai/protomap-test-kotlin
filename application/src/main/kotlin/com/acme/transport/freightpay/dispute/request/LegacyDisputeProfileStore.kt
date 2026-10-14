package com.acme.transport.freightpay.dispute.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy dispute profiles by code. */
interface LegacyDisputeProfileStore {
    fun find(code: String): LegacyDisputeProfile?
    fun save(item: LegacyDisputeProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDisputeProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDisputeProfileStore(
    var items: MutableMap<String, LegacyDisputeProfile>,
    var order: MutableList<String>,
) : LegacyDisputeProfileStore {
    override fun find(code: String): LegacyDisputeProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDisputeProfile) {
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

    override fun list(): MutableList<LegacyDisputeProfile> {
        val out = mutableListOf<LegacyDisputeProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDisputeProfileStore(): LegacyDisputeProfileStore {
    return MemoryLegacyDisputeProfileStore(
        mutableMapOf<String, LegacyDisputeProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDisputeProfileStore(
    var inner: LegacyDisputeProfileStore,
    var cache: TtlCache<LegacyDisputeProfile>,
    var hits: Int,
    var misses: Int,
) : LegacyDisputeProfileStore {
    override fun find(code: String): LegacyDisputeProfile? {
        val hit = (cache.get(code) as LegacyDisputeProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDisputeProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDisputeProfile> = inner.list()
}

fun newCachedLegacyDisputeProfileStore(inner: LegacyDisputeProfileStore, ttl: Int, clock: Clock): LegacyDisputeProfileStore {
    return CachedLegacyDisputeProfileStore(
        inner = inner,
        cache = newTtlCache<LegacyDisputeProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy dispute profiles. Empty fields match all. */
data class LegacyDisputeProfileQuery(
    var channel: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDisputeProfile): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<LegacyDisputeProfile>): MutableList<LegacyDisputeProfile> {
        val matched = filter(items) { candidate: LegacyDisputeProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDisputeProfiles(store: LegacyDisputeProfileStore, query: LegacyDisputeProfileQuery): MutableList<LegacyDisputeProfile> {
    return query.apply(store.list())
}
