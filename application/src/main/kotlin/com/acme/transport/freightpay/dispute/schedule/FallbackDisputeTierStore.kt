package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback dispute tiers by code. */
interface FallbackDisputeTierStore {
    fun find(code: String): FallbackDisputeTier?
    fun save(item: FallbackDisputeTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDisputeTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDisputeTierStore(
    var items: MutableMap<String, FallbackDisputeTier>,
    var order: MutableList<String>,
) : FallbackDisputeTierStore {
    override fun find(code: String): FallbackDisputeTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDisputeTier) {
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

    override fun list(): MutableList<FallbackDisputeTier> {
        val out = mutableListOf<FallbackDisputeTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDisputeTierStore(): FallbackDisputeTierStore {
    return MemoryFallbackDisputeTierStore(mutableMapOf<String, FallbackDisputeTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDisputeTierStore(
    var inner: FallbackDisputeTierStore,
    var cache: TtlCache<FallbackDisputeTier>,
    var hits: Int,
    var misses: Int,
) : FallbackDisputeTierStore {
    override fun find(code: String): FallbackDisputeTier? {
        val hit = (cache.get(code) as FallbackDisputeTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDisputeTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDisputeTier> = inner.list()
}

fun newCachedFallbackDisputeTierStore(inner: FallbackDisputeTierStore, ttl: Int, clock: Clock): FallbackDisputeTierStore {
    return CachedFallbackDisputeTierStore(
        inner = inner,
        cache = newTtlCache<FallbackDisputeTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dispute tiers. Empty fields match all. */
data class FallbackDisputeTierQuery(
    var locale: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDisputeTier): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<FallbackDisputeTier>): MutableList<FallbackDisputeTier> {
        val matched = filter(items) { candidate: FallbackDisputeTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDisputeTiers(store: FallbackDisputeTierStore, query: FallbackDisputeTierQuery): MutableList<FallbackDisputeTier> {
    return query.apply(store.list())
}
