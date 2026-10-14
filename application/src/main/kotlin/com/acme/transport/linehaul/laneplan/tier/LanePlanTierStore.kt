package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan tiers by code. */
interface LanePlanTierStore {
    fun find(code: String): LanePlanTier?
    fun save(item: LanePlanTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanTierStore(
    var items: MutableMap<String, LanePlanTier>,
    var order: MutableList<String>,
) : LanePlanTierStore {
    override fun find(code: String): LanePlanTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanTier) {
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

    override fun list(): MutableList<LanePlanTier> {
        val out = mutableListOf<LanePlanTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanTierStore(): LanePlanTierStore {
    return MemoryLanePlanTierStore(mutableMapOf<String, LanePlanTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanTierStore(
    var inner: LanePlanTierStore,
    var cache: TtlCache<LanePlanTier>,
    var hits: Int,
    var misses: Int,
) : LanePlanTierStore {
    override fun find(code: String): LanePlanTier? {
        val hit = (cache.get(code) as LanePlanTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanTier> = inner.list()
}

fun newCachedLanePlanTierStore(inner: LanePlanTierStore, ttl: Int, clock: Clock): LanePlanTierStore {
    return CachedLanePlanTierStore(
        inner = inner,
        cache = newTtlCache<LanePlanTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan tiers. Empty fields match all. */
data class LanePlanTierQuery(
    var source: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanTier): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<LanePlanTier>): MutableList<LanePlanTier> {
        val matched = filter(items) { candidate: LanePlanTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanTiers(store: LanePlanTierStore, query: LanePlanTierQuery): MutableList<LanePlanTier> {
    return query.apply(store.list())
}
