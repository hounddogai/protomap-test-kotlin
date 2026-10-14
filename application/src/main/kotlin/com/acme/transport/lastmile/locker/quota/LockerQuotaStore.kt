package com.acme.transport.lastmile.locker.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker quotas by code. */
interface LockerQuotaStore {
    fun find(code: String): LockerQuota?
    fun save(item: LockerQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerQuotaStore(
    var items: MutableMap<String, LockerQuota>,
    var order: MutableList<String>,
) : LockerQuotaStore {
    override fun find(code: String): LockerQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerQuota) {
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

    override fun list(): MutableList<LockerQuota> {
        val out = mutableListOf<LockerQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerQuotaStore(): LockerQuotaStore {
    return MemoryLockerQuotaStore(mutableMapOf<String, LockerQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerQuotaStore(
    var inner: LockerQuotaStore,
    var cache: TtlCache<LockerQuota>,
    var hits: Int,
    var misses: Int,
) : LockerQuotaStore {
    override fun find(code: String): LockerQuota? {
        val hit = (cache.get(code) as LockerQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerQuota> = inner.list()
}

fun newCachedLockerQuotaStore(inner: LockerQuotaStore, ttl: Int, clock: Clock): LockerQuotaStore {
    return CachedLockerQuotaStore(
        inner = inner,
        cache = newTtlCache<LockerQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker quotas. Empty fields match all. */
data class LockerQuotaQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerQuota>): MutableList<LockerQuota> {
        val matched = filter(items) { candidate: LockerQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerQuotas(store: LockerQuotaStore, query: LockerQuotaQuery): MutableList<LockerQuota> {
    return query.apply(store.list())
}
