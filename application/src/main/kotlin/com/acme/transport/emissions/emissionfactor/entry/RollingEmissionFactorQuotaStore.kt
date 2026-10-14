package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling emission factor quotas by code. */
interface RollingEmissionFactorQuotaStore {
    fun find(code: String): RollingEmissionFactorQuota?
    fun save(item: RollingEmissionFactorQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingEmissionFactorQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingEmissionFactorQuotaStore(
    var items: MutableMap<String, RollingEmissionFactorQuota>,
    var order: MutableList<String>,
) : RollingEmissionFactorQuotaStore {
    override fun find(code: String): RollingEmissionFactorQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingEmissionFactorQuota) {
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

    override fun list(): MutableList<RollingEmissionFactorQuota> {
        val out = mutableListOf<RollingEmissionFactorQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingEmissionFactorQuotaStore(): RollingEmissionFactorQuotaStore {
    return MemoryRollingEmissionFactorQuotaStore(
        mutableMapOf<String, RollingEmissionFactorQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingEmissionFactorQuotaStore(
    var inner: RollingEmissionFactorQuotaStore,
    var cache: TtlCache<RollingEmissionFactorQuota>,
    var hits: Int,
    var misses: Int,
) : RollingEmissionFactorQuotaStore {
    override fun find(code: String): RollingEmissionFactorQuota? {
        val hit = (cache.get(code) as RollingEmissionFactorQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingEmissionFactorQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingEmissionFactorQuota> = inner.list()
}

fun newCachedRollingEmissionFactorQuotaStore(inner: RollingEmissionFactorQuotaStore, ttl: Int, clock: Clock): RollingEmissionFactorQuotaStore {
    return CachedRollingEmissionFactorQuotaStore(
        inner = inner,
        cache = newTtlCache<RollingEmissionFactorQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling emission factor quotas. Empty fields match all. */
data class RollingEmissionFactorQuotaQuery(
    var locale: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingEmissionFactorQuota): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingEmissionFactorQuota>): MutableList<RollingEmissionFactorQuota> {
        val matched = filter(items) { candidate: RollingEmissionFactorQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingEmissionFactorQuotas(store: RollingEmissionFactorQuotaStore, query: RollingEmissionFactorQuotaQuery): MutableList<RollingEmissionFactorQuota> {
    return query.apply(store.list())
}
