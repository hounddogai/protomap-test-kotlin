package com.acme.transport.safety.violation.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary violation tiers by code. */
interface PrimaryViolationTierStore {
    fun find(code: String): PrimaryViolationTier?
    fun save(item: PrimaryViolationTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryViolationTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryViolationTierStore(
    var items: MutableMap<String, PrimaryViolationTier>,
    var order: MutableList<String>,
) : PrimaryViolationTierStore {
    override fun find(code: String): PrimaryViolationTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryViolationTier) {
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

    override fun list(): MutableList<PrimaryViolationTier> {
        val out = mutableListOf<PrimaryViolationTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryViolationTierStore(): PrimaryViolationTierStore {
    return MemoryPrimaryViolationTierStore(
        mutableMapOf<String, PrimaryViolationTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryViolationTierStore(
    var inner: PrimaryViolationTierStore,
    var cache: TtlCache<PrimaryViolationTier>,
    var hits: Int,
    var misses: Int,
) : PrimaryViolationTierStore {
    override fun find(code: String): PrimaryViolationTier? {
        val hit = (cache.get(code) as PrimaryViolationTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryViolationTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryViolationTier> = inner.list()
}

fun newCachedPrimaryViolationTierStore(inner: PrimaryViolationTierStore, ttl: Int, clock: Clock): PrimaryViolationTierStore {
    return CachedPrimaryViolationTierStore(
        inner = inner,
        cache = newTtlCache<PrimaryViolationTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary violation tiers. Empty fields match all. */
data class PrimaryViolationTierQuery(
    var segment: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryViolationTier): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<PrimaryViolationTier>): MutableList<PrimaryViolationTier> {
        val matched = filter(items) { candidate: PrimaryViolationTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryViolationTiers(store: PrimaryViolationTierStore, query: PrimaryViolationTierQuery): MutableList<PrimaryViolationTier> {
    return query.apply(store.list())
}
