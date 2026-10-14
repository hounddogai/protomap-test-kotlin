package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation tiers by code. */
interface ViolationTierStore {
    fun find(code: String): ViolationTier?
    fun save(item: ViolationTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationTierStore(
    var items: MutableMap<String, ViolationTier>,
    var order: MutableList<String>,
) : ViolationTierStore {
    override fun find(code: String): ViolationTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationTier) {
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

    override fun list(): MutableList<ViolationTier> {
        val out = mutableListOf<ViolationTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationTierStore(): ViolationTierStore {
    return MemoryViolationTierStore(mutableMapOf<String, ViolationTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationTierStore(
    var inner: ViolationTierStore,
    var cache: TtlCache<ViolationTier>,
    var hits: Int,
    var misses: Int,
) : ViolationTierStore {
    override fun find(code: String): ViolationTier? {
        val hit = (cache.get(code) as ViolationTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationTier> = inner.list()
}

fun newCachedViolationTierStore(inner: ViolationTierStore, ttl: Int, clock: Clock): ViolationTierStore {
    return CachedViolationTierStore(
        inner = inner,
        cache = newTtlCache<ViolationTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation tiers. Empty fields match all. */
data class ViolationTierQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationTier>): MutableList<ViolationTier> {
        val matched = filter(items) { candidate: ViolationTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationTiers(store: ViolationTierStore, query: ViolationTierQuery): MutableList<ViolationTier> {
    return query.apply(store.list())
}
