package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt quotas by code. */
interface AttemptQuotaStore {
    fun find(code: String): AttemptQuota?
    fun save(item: AttemptQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptQuotaStore(
    var items: MutableMap<String, AttemptQuota>,
    var order: MutableList<String>,
) : AttemptQuotaStore {
    override fun find(code: String): AttemptQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptQuota) {
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

    override fun list(): MutableList<AttemptQuota> {
        val out = mutableListOf<AttemptQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptQuotaStore(): AttemptQuotaStore {
    return MemoryAttemptQuotaStore(mutableMapOf<String, AttemptQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptQuotaStore(
    var inner: AttemptQuotaStore,
    var cache: TtlCache<AttemptQuota>,
    var hits: Int,
    var misses: Int,
) : AttemptQuotaStore {
    override fun find(code: String): AttemptQuota? {
        val hit = (cache.get(code) as AttemptQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptQuota> = inner.list()
}

fun newCachedAttemptQuotaStore(inner: AttemptQuotaStore, ttl: Int, clock: Clock): AttemptQuotaStore {
    return CachedAttemptQuotaStore(
        inner = inner,
        cache = newTtlCache<AttemptQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt quotas. Empty fields match all. */
data class AttemptQuotaQuery(
    var unit: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptQuota): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<AttemptQuota>): MutableList<AttemptQuota> {
        val matched = filter(items) { candidate: AttemptQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptQuotas(store: AttemptQuotaStore, query: AttemptQuotaQuery): MutableList<AttemptQuota> {
    return query.apply(store.list())
}
