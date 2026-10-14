package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary fuel surcharge tiers by code. */
interface PrimaryFuelSurchargeTierStore {
    fun find(code: String): PrimaryFuelSurchargeTier?
    fun save(item: PrimaryFuelSurchargeTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryFuelSurchargeTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryFuelSurchargeTierStore(
    var items: MutableMap<String, PrimaryFuelSurchargeTier>,
    var order: MutableList<String>,
) : PrimaryFuelSurchargeTierStore {
    override fun find(code: String): PrimaryFuelSurchargeTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryFuelSurchargeTier) {
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

    override fun list(): MutableList<PrimaryFuelSurchargeTier> {
        val out = mutableListOf<PrimaryFuelSurchargeTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryFuelSurchargeTierStore(): PrimaryFuelSurchargeTierStore {
    return MemoryPrimaryFuelSurchargeTierStore(
        mutableMapOf<String, PrimaryFuelSurchargeTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryFuelSurchargeTierStore(
    var inner: PrimaryFuelSurchargeTierStore,
    var cache: TtlCache<PrimaryFuelSurchargeTier>,
    var hits: Int,
    var misses: Int,
) : PrimaryFuelSurchargeTierStore {
    override fun find(code: String): PrimaryFuelSurchargeTier? {
        val hit = (cache.get(code) as PrimaryFuelSurchargeTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryFuelSurchargeTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryFuelSurchargeTier> = inner.list()
}

fun newCachedPrimaryFuelSurchargeTierStore(inner: PrimaryFuelSurchargeTierStore, ttl: Int, clock: Clock): PrimaryFuelSurchargeTierStore {
    return CachedPrimaryFuelSurchargeTierStore(
        inner = inner,
        cache = newTtlCache<PrimaryFuelSurchargeTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary fuel surcharge tiers. Empty fields match all. */
data class PrimaryFuelSurchargeTierQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryFuelSurchargeTier): Boolean {
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

    fun apply(items: MutableList<PrimaryFuelSurchargeTier>): MutableList<PrimaryFuelSurchargeTier> {
        val matched = filter(items) { candidate: PrimaryFuelSurchargeTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryFuelSurchargeTiers(store: PrimaryFuelSurchargeTierStore, query: PrimaryFuelSurchargeTierQuery): MutableList<PrimaryFuelSurchargeTier> {
    return query.apply(store.list())
}
