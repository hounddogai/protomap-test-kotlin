package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy accessorial tiers by code. */
interface LegacyAccessorialTierStore {
    fun find(code: String): LegacyAccessorialTier?
    fun save(item: LegacyAccessorialTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAccessorialTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAccessorialTierStore(
    var items: MutableMap<String, LegacyAccessorialTier>,
    var order: MutableList<String>,
) : LegacyAccessorialTierStore {
    override fun find(code: String): LegacyAccessorialTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAccessorialTier) {
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

    override fun list(): MutableList<LegacyAccessorialTier> {
        val out = mutableListOf<LegacyAccessorialTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAccessorialTierStore(): LegacyAccessorialTierStore {
    return MemoryLegacyAccessorialTierStore(
        mutableMapOf<String, LegacyAccessorialTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAccessorialTierStore(
    var inner: LegacyAccessorialTierStore,
    var cache: TtlCache<LegacyAccessorialTier>,
    var hits: Int,
    var misses: Int,
) : LegacyAccessorialTierStore {
    override fun find(code: String): LegacyAccessorialTier? {
        val hit = (cache.get(code) as LegacyAccessorialTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAccessorialTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAccessorialTier> = inner.list()
}

fun newCachedLegacyAccessorialTierStore(inner: LegacyAccessorialTierStore, ttl: Int, clock: Clock): LegacyAccessorialTierStore {
    return CachedLegacyAccessorialTierStore(
        inner = inner,
        cache = newTtlCache<LegacyAccessorialTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy accessorial tiers. Empty fields match all. */
data class LegacyAccessorialTierQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAccessorialTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyAccessorialTier>): MutableList<LegacyAccessorialTier> {
        val matched = filter(items) { candidate: LegacyAccessorialTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAccessorialTiers(store: LegacyAccessorialTierStore, query: LegacyAccessorialTierQuery): MutableList<LegacyAccessorialTier> {
    return query.apply(store.list())
}
