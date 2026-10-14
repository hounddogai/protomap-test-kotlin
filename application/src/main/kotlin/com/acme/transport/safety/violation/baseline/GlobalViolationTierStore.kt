package com.acme.transport.safety.violation.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global violation tiers by code. */
interface GlobalViolationTierStore {
    fun find(code: String): GlobalViolationTier?
    fun save(item: GlobalViolationTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalViolationTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalViolationTierStore(
    var items: MutableMap<String, GlobalViolationTier>,
    var order: MutableList<String>,
) : GlobalViolationTierStore {
    override fun find(code: String): GlobalViolationTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalViolationTier) {
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

    override fun list(): MutableList<GlobalViolationTier> {
        val out = mutableListOf<GlobalViolationTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalViolationTierStore(): GlobalViolationTierStore {
    return MemoryGlobalViolationTierStore(mutableMapOf<String, GlobalViolationTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalViolationTierStore(
    var inner: GlobalViolationTierStore,
    var cache: TtlCache<GlobalViolationTier>,
    var hits: Int,
    var misses: Int,
) : GlobalViolationTierStore {
    override fun find(code: String): GlobalViolationTier? {
        val hit = (cache.get(code) as GlobalViolationTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalViolationTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalViolationTier> = inner.list()
}

fun newCachedGlobalViolationTierStore(inner: GlobalViolationTierStore, ttl: Int, clock: Clock): GlobalViolationTierStore {
    return CachedGlobalViolationTierStore(
        inner = inner,
        cache = newTtlCache<GlobalViolationTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global violation tiers. Empty fields match all. */
data class GlobalViolationTierQuery(
    var lane: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalViolationTier): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalViolationTier>): MutableList<GlobalViolationTier> {
        val matched = filter(items) { candidate: GlobalViolationTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalViolationTiers(store: GlobalViolationTierStore, query: GlobalViolationTierQuery): MutableList<GlobalViolationTier> {
    return query.apply(store.list())
}
