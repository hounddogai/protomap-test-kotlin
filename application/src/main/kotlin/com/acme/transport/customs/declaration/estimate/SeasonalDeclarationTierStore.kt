package com.acme.transport.customs.declaration.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal declaration tiers by code. */
interface SeasonalDeclarationTierStore {
    fun find(code: String): SeasonalDeclarationTier?
    fun save(item: SeasonalDeclarationTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDeclarationTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDeclarationTierStore(
    var items: MutableMap<String, SeasonalDeclarationTier>,
    var order: MutableList<String>,
) : SeasonalDeclarationTierStore {
    override fun find(code: String): SeasonalDeclarationTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDeclarationTier) {
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

    override fun list(): MutableList<SeasonalDeclarationTier> {
        val out = mutableListOf<SeasonalDeclarationTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDeclarationTierStore(): SeasonalDeclarationTierStore {
    return MemorySeasonalDeclarationTierStore(
        mutableMapOf<String, SeasonalDeclarationTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDeclarationTierStore(
    var inner: SeasonalDeclarationTierStore,
    var cache: TtlCache<SeasonalDeclarationTier>,
    var hits: Int,
    var misses: Int,
) : SeasonalDeclarationTierStore {
    override fun find(code: String): SeasonalDeclarationTier? {
        val hit = (cache.get(code) as SeasonalDeclarationTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDeclarationTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDeclarationTier> = inner.list()
}

fun newCachedSeasonalDeclarationTierStore(inner: SeasonalDeclarationTierStore, ttl: Int, clock: Clock): SeasonalDeclarationTierStore {
    return CachedSeasonalDeclarationTierStore(
        inner = inner,
        cache = newTtlCache<SeasonalDeclarationTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal declaration tiers. Empty fields match all. */
data class SeasonalDeclarationTierQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDeclarationTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDeclarationTier>): MutableList<SeasonalDeclarationTier> {
        val matched = filter(items) { candidate: SeasonalDeclarationTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDeclarationTiers(store: SeasonalDeclarationTierStore, query: SeasonalDeclarationTierQuery): MutableList<SeasonalDeclarationTier> {
    return query.apply(store.list())
}
