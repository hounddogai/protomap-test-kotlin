package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard quotas by code. */
interface ScorecardQuotaStore {
    fun find(code: String): ScorecardQuota?
    fun save(item: ScorecardQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardQuotaStore(
    var items: MutableMap<String, ScorecardQuota>,
    var order: MutableList<String>,
) : ScorecardQuotaStore {
    override fun find(code: String): ScorecardQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardQuota) {
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

    override fun list(): MutableList<ScorecardQuota> {
        val out = mutableListOf<ScorecardQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardQuotaStore(): ScorecardQuotaStore {
    return MemoryScorecardQuotaStore(mutableMapOf<String, ScorecardQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardQuotaStore(
    var inner: ScorecardQuotaStore,
    var cache: TtlCache<ScorecardQuota>,
    var hits: Int,
    var misses: Int,
) : ScorecardQuotaStore {
    override fun find(code: String): ScorecardQuota? {
        val hit = (cache.get(code) as ScorecardQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardQuota> = inner.list()
}

fun newCachedScorecardQuotaStore(inner: ScorecardQuotaStore, ttl: Int, clock: Clock): ScorecardQuotaStore {
    return CachedScorecardQuotaStore(
        inner = inner,
        cache = newTtlCache<ScorecardQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard quotas. Empty fields match all. */
data class ScorecardQuotaQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ScorecardQuota>): MutableList<ScorecardQuota> {
        val matched = filter(items) { candidate: ScorecardQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardQuotas(store: ScorecardQuotaStore, query: ScorecardQuotaQuery): MutableList<ScorecardQuota> {
    return query.apply(store.list())
}
