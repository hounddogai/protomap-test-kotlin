package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop quotas by code. */
interface DropQuotaStore {
    fun find(code: String): DropQuota?
    fun save(item: DropQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropQuotaStore(
    var items: MutableMap<String, DropQuota>,
    var order: MutableList<String>,
) : DropQuotaStore {
    override fun find(code: String): DropQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropQuota) {
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

    override fun list(): MutableList<DropQuota> {
        val out = mutableListOf<DropQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropQuotaStore(): DropQuotaStore {
    return MemoryDropQuotaStore(mutableMapOf<String, DropQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropQuotaStore(
    var inner: DropQuotaStore,
    var cache: TtlCache<DropQuota>,
    var hits: Int,
    var misses: Int,
) : DropQuotaStore {
    override fun find(code: String): DropQuota? {
        val hit = (cache.get(code) as DropQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropQuota> = inner.list()
}

fun newCachedDropQuotaStore(inner: DropQuotaStore, ttl: Int, clock: Clock): DropQuotaStore {
    return CachedDropQuotaStore(
        inner = inner,
        cache = newTtlCache<DropQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop quotas. Empty fields match all. */
data class DropQuotaQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropQuota): Boolean {
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

    fun apply(items: MutableList<DropQuota>): MutableList<DropQuota> {
        val matched = filter(items) { candidate: DropQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropQuotas(store: DropQuotaStore, query: DropQuotaQuery): MutableList<DropQuota> {
    return query.apply(store.list())
}
