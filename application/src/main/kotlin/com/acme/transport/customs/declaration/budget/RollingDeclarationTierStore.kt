package com.acme.transport.customs.declaration.budget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling declaration tiers by code. */
interface RollingDeclarationTierStore {
    fun find(code: String): RollingDeclarationTier?
    fun save(item: RollingDeclarationTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDeclarationTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDeclarationTierStore(
    var items: MutableMap<String, RollingDeclarationTier>,
    var order: MutableList<String>,
) : RollingDeclarationTierStore {
    override fun find(code: String): RollingDeclarationTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDeclarationTier) {
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

    override fun list(): MutableList<RollingDeclarationTier> {
        val out = mutableListOf<RollingDeclarationTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDeclarationTierStore(): RollingDeclarationTierStore {
    return MemoryRollingDeclarationTierStore(
        mutableMapOf<String, RollingDeclarationTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDeclarationTierStore(
    var inner: RollingDeclarationTierStore,
    var cache: TtlCache<RollingDeclarationTier>,
    var hits: Int,
    var misses: Int,
) : RollingDeclarationTierStore {
    override fun find(code: String): RollingDeclarationTier? {
        val hit = (cache.get(code) as RollingDeclarationTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDeclarationTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDeclarationTier> = inner.list()
}

fun newCachedRollingDeclarationTierStore(inner: RollingDeclarationTierStore, ttl: Int, clock: Clock): RollingDeclarationTierStore {
    return CachedRollingDeclarationTierStore(
        inner = inner,
        cache = newTtlCache<RollingDeclarationTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling declaration tiers. Empty fields match all. */
data class RollingDeclarationTierQuery(
    var source: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDeclarationTier): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RollingDeclarationTier>): MutableList<RollingDeclarationTier> {
        val matched = filter(items) { candidate: RollingDeclarationTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDeclarationTiers(store: RollingDeclarationTierStore, query: RollingDeclarationTierQuery): MutableList<RollingDeclarationTier> {
    return query.apply(store.list())
}
