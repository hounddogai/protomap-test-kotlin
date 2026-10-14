package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary fuel surcharge drafts by code. */
interface PrimaryFuelSurchargeDraftStore {
    fun find(code: String): PrimaryFuelSurchargeDraft?
    fun save(item: PrimaryFuelSurchargeDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryFuelSurchargeDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryFuelSurchargeDraftStore(
    var items: MutableMap<String, PrimaryFuelSurchargeDraft>,
    var order: MutableList<String>,
) : PrimaryFuelSurchargeDraftStore {
    override fun find(code: String): PrimaryFuelSurchargeDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryFuelSurchargeDraft) {
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

    override fun list(): MutableList<PrimaryFuelSurchargeDraft> {
        val out = mutableListOf<PrimaryFuelSurchargeDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryFuelSurchargeDraftStore(): PrimaryFuelSurchargeDraftStore {
    return MemoryPrimaryFuelSurchargeDraftStore(
        mutableMapOf<String, PrimaryFuelSurchargeDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryFuelSurchargeDraftStore(
    var inner: PrimaryFuelSurchargeDraftStore,
    var cache: TtlCache<PrimaryFuelSurchargeDraft>,
    var hits: Int,
    var misses: Int,
) : PrimaryFuelSurchargeDraftStore {
    override fun find(code: String): PrimaryFuelSurchargeDraft? {
        val hit = (cache.get(code) as PrimaryFuelSurchargeDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryFuelSurchargeDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryFuelSurchargeDraft> = inner.list()
}

fun newCachedPrimaryFuelSurchargeDraftStore(inner: PrimaryFuelSurchargeDraftStore, ttl: Int, clock: Clock): PrimaryFuelSurchargeDraftStore {
    return CachedPrimaryFuelSurchargeDraftStore(
        inner = inner,
        cache = newTtlCache<PrimaryFuelSurchargeDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary fuel surcharge drafts. Empty fields match all. */
data class PrimaryFuelSurchargeDraftQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryFuelSurchargeDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryFuelSurchargeDraft>): MutableList<PrimaryFuelSurchargeDraft> {
        val matched = filter(items) { candidate: PrimaryFuelSurchargeDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryFuelSurchargeDrafts(store: PrimaryFuelSurchargeDraftStore, query: PrimaryFuelSurchargeDraftQuery): MutableList<PrimaryFuelSurchargeDraft> {
    return query.apply(store.list())
}
