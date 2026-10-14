package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling node tiers by code. */
interface RollingNodeTierStore {
    fun find(code: String): RollingNodeTier?
    fun save(item: RollingNodeTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingNodeTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingNodeTierStore(
    var items: MutableMap<String, RollingNodeTier>,
    var order: MutableList<String>,
) : RollingNodeTierStore {
    override fun find(code: String): RollingNodeTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingNodeTier) {
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

    override fun list(): MutableList<RollingNodeTier> {
        val out = mutableListOf<RollingNodeTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingNodeTierStore(): RollingNodeTierStore {
    return MemoryRollingNodeTierStore(mutableMapOf<String, RollingNodeTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingNodeTierStore(
    var inner: RollingNodeTierStore,
    var cache: TtlCache<RollingNodeTier>,
    var hits: Int,
    var misses: Int,
) : RollingNodeTierStore {
    override fun find(code: String): RollingNodeTier? {
        val hit = (cache.get(code) as RollingNodeTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingNodeTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingNodeTier> = inner.list()
}

fun newCachedRollingNodeTierStore(inner: RollingNodeTierStore, ttl: Int, clock: Clock): RollingNodeTierStore {
    return CachedRollingNodeTierStore(
        inner = inner,
        cache = newTtlCache<RollingNodeTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling node tiers. Empty fields match all. */
data class RollingNodeTierQuery(
    var segment: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingNodeTier): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingNodeTier>): MutableList<RollingNodeTier> {
        val matched = filter(items) { candidate: RollingNodeTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingNodeTiers(store: RollingNodeTierStore, query: RollingNodeTierQuery): MutableList<RollingNodeTier> {
    return query.apply(store.list())
}
