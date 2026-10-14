package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional zone tiers by code. */
interface RegionalZoneTierStore {
    fun find(code: String): RegionalZoneTier?
    fun save(item: RegionalZoneTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalZoneTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalZoneTierStore(
    var items: MutableMap<String, RegionalZoneTier>,
    var order: MutableList<String>,
) : RegionalZoneTierStore {
    override fun find(code: String): RegionalZoneTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalZoneTier) {
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

    override fun list(): MutableList<RegionalZoneTier> {
        val out = mutableListOf<RegionalZoneTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalZoneTierStore(): RegionalZoneTierStore {
    return MemoryRegionalZoneTierStore(mutableMapOf<String, RegionalZoneTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalZoneTierStore(
    var inner: RegionalZoneTierStore,
    var cache: TtlCache<RegionalZoneTier>,
    var hits: Int,
    var misses: Int,
) : RegionalZoneTierStore {
    override fun find(code: String): RegionalZoneTier? {
        val hit = (cache.get(code) as RegionalZoneTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalZoneTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalZoneTier> = inner.list()
}

fun newCachedRegionalZoneTierStore(inner: RegionalZoneTierStore, ttl: Int, clock: Clock): RegionalZoneTierStore {
    return CachedRegionalZoneTierStore(
        inner = inner,
        cache = newTtlCache<RegionalZoneTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional zone tiers. Empty fields match all. */
data class RegionalZoneTierQuery(
    var segment: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalZoneTier): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalZoneTier>): MutableList<RegionalZoneTier> {
        val matched = filter(items) { candidate: RegionalZoneTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalZoneTiers(store: RegionalZoneTierStore, query: RegionalZoneTierQuery): MutableList<RegionalZoneTier> {
    return query.apply(store.list())
}
