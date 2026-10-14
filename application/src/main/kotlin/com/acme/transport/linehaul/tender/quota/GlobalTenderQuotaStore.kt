package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global tender quotas by code. */
interface GlobalTenderQuotaStore {
    fun find(code: String): GlobalTenderQuota?
    fun save(item: GlobalTenderQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTenderQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTenderQuotaStore(
    var items: MutableMap<String, GlobalTenderQuota>,
    var order: MutableList<String>,
) : GlobalTenderQuotaStore {
    override fun find(code: String): GlobalTenderQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTenderQuota) {
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

    override fun list(): MutableList<GlobalTenderQuota> {
        val out = mutableListOf<GlobalTenderQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTenderQuotaStore(): GlobalTenderQuotaStore {
    return MemoryGlobalTenderQuotaStore(mutableMapOf<String, GlobalTenderQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTenderQuotaStore(
    var inner: GlobalTenderQuotaStore,
    var cache: TtlCache<GlobalTenderQuota>,
    var hits: Int,
    var misses: Int,
) : GlobalTenderQuotaStore {
    override fun find(code: String): GlobalTenderQuota? {
        val hit = (cache.get(code) as GlobalTenderQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTenderQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTenderQuota> = inner.list()
}

fun newCachedGlobalTenderQuotaStore(inner: GlobalTenderQuotaStore, ttl: Int, clock: Clock): GlobalTenderQuotaStore {
    return CachedGlobalTenderQuotaStore(
        inner = inner,
        cache = newTtlCache<GlobalTenderQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global tender quotas. Empty fields match all. */
data class GlobalTenderQuotaQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTenderQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<GlobalTenderQuota>): MutableList<GlobalTenderQuota> {
        val matched = filter(items) { candidate: GlobalTenderQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTenderQuotas(store: GlobalTenderQuotaStore, query: GlobalTenderQuotaQuery): MutableList<GlobalTenderQuota> {
    return query.apply(store.list())
}
