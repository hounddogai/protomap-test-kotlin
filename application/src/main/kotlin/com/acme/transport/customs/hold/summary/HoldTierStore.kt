package com.acme.transport.customs.hold.summary

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold tiers by code. */
interface HoldTierStore {
    fun find(code: String): HoldTier?
    fun save(item: HoldTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldTierStore(
    var items: MutableMap<String, HoldTier>,
    var order: MutableList<String>,
) : HoldTierStore {
    override fun find(code: String): HoldTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldTier) {
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

    override fun list(): MutableList<HoldTier> {
        val out = mutableListOf<HoldTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldTierStore(): HoldTierStore {
    return MemoryHoldTierStore(mutableMapOf<String, HoldTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldTierStore(
    var inner: HoldTierStore,
    var cache: TtlCache<HoldTier>,
    var hits: Int,
    var misses: Int,
) : HoldTierStore {
    override fun find(code: String): HoldTier? {
        val hit = (cache.get(code) as HoldTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldTier> = inner.list()
}

fun newCachedHoldTierStore(inner: HoldTierStore, ttl: Int, clock: Clock): HoldTierStore {
    return CachedHoldTierStore(
        inner = inner,
        cache = newTtlCache<HoldTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold tiers. Empty fields match all. */
data class HoldTierQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldTier>): MutableList<HoldTier> {
        val matched = filter(items) { candidate: HoldTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldTiers(store: HoldTierStore, query: HoldTierQuery): MutableList<HoldTier> = query.apply(store.list())
