package com.acme.transport.customs.declaration.plan

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration quotas by code. */
interface DeclarationQuotaStore {
    fun find(code: String): DeclarationQuota?
    fun save(item: DeclarationQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationQuotaStore(
    var items: MutableMap<String, DeclarationQuota>,
    var order: MutableList<String>,
) : DeclarationQuotaStore {
    override fun find(code: String): DeclarationQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationQuota) {
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

    override fun list(): MutableList<DeclarationQuota> {
        val out = mutableListOf<DeclarationQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationQuotaStore(): DeclarationQuotaStore {
    return MemoryDeclarationQuotaStore(mutableMapOf<String, DeclarationQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationQuotaStore(
    var inner: DeclarationQuotaStore,
    var cache: TtlCache<DeclarationQuota>,
    var hits: Int,
    var misses: Int,
) : DeclarationQuotaStore {
    override fun find(code: String): DeclarationQuota? {
        val hit = (cache.get(code) as DeclarationQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationQuota> = inner.list()
}

fun newCachedDeclarationQuotaStore(inner: DeclarationQuotaStore, ttl: Int, clock: Clock): DeclarationQuotaStore {
    return CachedDeclarationQuotaStore(
        inner = inner,
        cache = newTtlCache<DeclarationQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration quotas. Empty fields match all. */
data class DeclarationQuotaQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationQuota): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationQuota>): MutableList<DeclarationQuota> {
        val matched = filter(items) { candidate: DeclarationQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationQuotas(store: DeclarationQuotaStore, query: DeclarationQuotaQuery): MutableList<DeclarationQuota> {
    return query.apply(store.list())
}
