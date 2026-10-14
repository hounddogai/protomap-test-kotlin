package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy dispute tiers by code. */
interface LegacyDisputeTierStore {
    fun find(code: String): LegacyDisputeTier?
    fun save(item: LegacyDisputeTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDisputeTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDisputeTierStore(
    var items: MutableMap<String, LegacyDisputeTier>,
    var order: MutableList<String>,
) : LegacyDisputeTierStore {
    override fun find(code: String): LegacyDisputeTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDisputeTier) {
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

    override fun list(): MutableList<LegacyDisputeTier> {
        val out = mutableListOf<LegacyDisputeTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDisputeTierStore(): LegacyDisputeTierStore {
    return MemoryLegacyDisputeTierStore(mutableMapOf<String, LegacyDisputeTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDisputeTierStore(
    var inner: LegacyDisputeTierStore,
    var cache: TtlCache<LegacyDisputeTier>,
    var hits: Int,
    var misses: Int,
) : LegacyDisputeTierStore {
    override fun find(code: String): LegacyDisputeTier? {
        val hit = (cache.get(code) as LegacyDisputeTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDisputeTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDisputeTier> = inner.list()
}

fun newCachedLegacyDisputeTierStore(inner: LegacyDisputeTierStore, ttl: Int, clock: Clock): LegacyDisputeTierStore {
    return CachedLegacyDisputeTierStore(
        inner = inner,
        cache = newTtlCache<LegacyDisputeTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy dispute tiers. Empty fields match all. */
data class LegacyDisputeTierQuery(
    var country: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDisputeTier): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDisputeTier>): MutableList<LegacyDisputeTier> {
        val matched = filter(items) { candidate: LegacyDisputeTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDisputeTiers(store: LegacyDisputeTierStore, query: LegacyDisputeTierQuery): MutableList<LegacyDisputeTier> {
    return query.apply(store.list())
}
