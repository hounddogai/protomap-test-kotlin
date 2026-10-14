package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default tire quotas by code. */
interface DefaultTireQuotaStore {
    fun find(code: String): DefaultTireQuota?
    fun save(item: DefaultTireQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTireQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTireQuotaStore(
    var items: MutableMap<String, DefaultTireQuota>,
    var order: MutableList<String>,
) : DefaultTireQuotaStore {
    override fun find(code: String): DefaultTireQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTireQuota) {
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

    override fun list(): MutableList<DefaultTireQuota> {
        val out = mutableListOf<DefaultTireQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTireQuotaStore(): DefaultTireQuotaStore {
    return MemoryDefaultTireQuotaStore(mutableMapOf<String, DefaultTireQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTireQuotaStore(
    var inner: DefaultTireQuotaStore,
    var cache: TtlCache<DefaultTireQuota>,
    var hits: Int,
    var misses: Int,
) : DefaultTireQuotaStore {
    override fun find(code: String): DefaultTireQuota? {
        val hit = (cache.get(code) as DefaultTireQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTireQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTireQuota> = inner.list()
}

fun newCachedDefaultTireQuotaStore(inner: DefaultTireQuotaStore, ttl: Int, clock: Clock): DefaultTireQuotaStore {
    return CachedDefaultTireQuotaStore(
        inner = inner,
        cache = newTtlCache<DefaultTireQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default tire quotas. Empty fields match all. */
data class DefaultTireQuotaQuery(
    var segment: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTireQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTireQuota>): MutableList<DefaultTireQuota> {
        val matched = filter(items) { candidate: DefaultTireQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTireQuotas(store: DefaultTireQuotaStore, query: DefaultTireQuotaQuery): MutableList<DefaultTireQuota> {
    return query.apply(store.list())
}
