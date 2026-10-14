package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge drafts by code. */
interface FuelSurchargeDraftStore {
    fun find(code: String): FuelSurchargeDraft?
    fun save(item: FuelSurchargeDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeDraftStore(
    var items: MutableMap<String, FuelSurchargeDraft>,
    var order: MutableList<String>,
) : FuelSurchargeDraftStore {
    override fun find(code: String): FuelSurchargeDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeDraft) {
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

    override fun list(): MutableList<FuelSurchargeDraft> {
        val out = mutableListOf<FuelSurchargeDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeDraftStore(): FuelSurchargeDraftStore {
    return MemoryFuelSurchargeDraftStore(mutableMapOf<String, FuelSurchargeDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeDraftStore(
    var inner: FuelSurchargeDraftStore,
    var cache: TtlCache<FuelSurchargeDraft>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeDraftStore {
    override fun find(code: String): FuelSurchargeDraft? {
        val hit = (cache.get(code) as FuelSurchargeDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeDraft> = inner.list()
}

fun newCachedFuelSurchargeDraftStore(inner: FuelSurchargeDraftStore, ttl: Int, clock: Clock): FuelSurchargeDraftStore {
    return CachedFuelSurchargeDraftStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge drafts. Empty fields match all. */
data class FuelSurchargeDraftQuery(
    var source: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeDraft): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeDraft>): MutableList<FuelSurchargeDraft> {
        val matched = filter(items) { candidate: FuelSurchargeDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeDrafts(store: FuelSurchargeDraftStore, query: FuelSurchargeDraftQuery): MutableList<FuelSurchargeDraft> {
    return query.apply(store.list())
}
