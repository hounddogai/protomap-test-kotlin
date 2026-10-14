package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling relay tiers by code. */
interface RollingRelayTierStore {
    fun find(code: String): RollingRelayTier?
    fun save(item: RollingRelayTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRelayTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRelayTierStore(
    var items: MutableMap<String, RollingRelayTier>,
    var order: MutableList<String>,
) : RollingRelayTierStore {
    override fun find(code: String): RollingRelayTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRelayTier) {
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

    override fun list(): MutableList<RollingRelayTier> {
        val out = mutableListOf<RollingRelayTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRelayTierStore(): RollingRelayTierStore {
    return MemoryRollingRelayTierStore(mutableMapOf<String, RollingRelayTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRelayTierStore(
    var inner: RollingRelayTierStore,
    var cache: TtlCache<RollingRelayTier>,
    var hits: Int,
    var misses: Int,
) : RollingRelayTierStore {
    override fun find(code: String): RollingRelayTier? {
        val hit = (cache.get(code) as RollingRelayTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRelayTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRelayTier> = inner.list()
}

fun newCachedRollingRelayTierStore(inner: RollingRelayTierStore, ttl: Int, clock: Clock): RollingRelayTierStore {
    return CachedRollingRelayTierStore(
        inner = inner,
        cache = newTtlCache<RollingRelayTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling relay tiers. Empty fields match all. */
data class RollingRelayTierQuery(
    var lane: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRelayTier): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<RollingRelayTier>): MutableList<RollingRelayTier> {
        val matched = filter(items) { candidate: RollingRelayTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRelayTiers(store: RollingRelayTierStore, query: RollingRelayTierQuery): MutableList<RollingRelayTier> {
    return query.apply(store.list())
}
