package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global cutoff tiers by code. */
interface GlobalCutoffTierStore {
    fun find(code: String): GlobalCutoffTier?
    fun save(item: GlobalCutoffTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCutoffTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCutoffTierStore(
    var items: MutableMap<String, GlobalCutoffTier>,
    var order: MutableList<String>,
) : GlobalCutoffTierStore {
    override fun find(code: String): GlobalCutoffTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCutoffTier) {
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

    override fun list(): MutableList<GlobalCutoffTier> {
        val out = mutableListOf<GlobalCutoffTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCutoffTierStore(): GlobalCutoffTierStore {
    return MemoryGlobalCutoffTierStore(mutableMapOf<String, GlobalCutoffTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCutoffTierStore(
    var inner: GlobalCutoffTierStore,
    var cache: TtlCache<GlobalCutoffTier>,
    var hits: Int,
    var misses: Int,
) : GlobalCutoffTierStore {
    override fun find(code: String): GlobalCutoffTier? {
        val hit = (cache.get(code) as GlobalCutoffTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCutoffTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCutoffTier> = inner.list()
}

fun newCachedGlobalCutoffTierStore(inner: GlobalCutoffTierStore, ttl: Int, clock: Clock): GlobalCutoffTierStore {
    return CachedGlobalCutoffTierStore(
        inner = inner,
        cache = newTtlCache<GlobalCutoffTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global cutoff tiers. Empty fields match all. */
data class GlobalCutoffTierQuery(
    var segment: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCutoffTier): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<GlobalCutoffTier>): MutableList<GlobalCutoffTier> {
        val matched = filter(items) { candidate: GlobalCutoffTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCutoffTiers(store: GlobalCutoffTierStore, query: GlobalCutoffTierQuery): MutableList<GlobalCutoffTier> {
    return query.apply(store.list())
}
