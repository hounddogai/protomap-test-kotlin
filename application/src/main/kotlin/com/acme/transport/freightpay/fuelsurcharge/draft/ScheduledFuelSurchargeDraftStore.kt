package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled fuel surcharge drafts by code. */
interface ScheduledFuelSurchargeDraftStore {
    fun find(code: String): ScheduledFuelSurchargeDraft?
    fun save(item: ScheduledFuelSurchargeDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledFuelSurchargeDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledFuelSurchargeDraftStore(
    var items: MutableMap<String, ScheduledFuelSurchargeDraft>,
    var order: MutableList<String>,
) : ScheduledFuelSurchargeDraftStore {
    override fun find(code: String): ScheduledFuelSurchargeDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledFuelSurchargeDraft) {
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

    override fun list(): MutableList<ScheduledFuelSurchargeDraft> {
        val out = mutableListOf<ScheduledFuelSurchargeDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledFuelSurchargeDraftStore(): ScheduledFuelSurchargeDraftStore {
    return MemoryScheduledFuelSurchargeDraftStore(
        mutableMapOf<String, ScheduledFuelSurchargeDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledFuelSurchargeDraftStore(
    var inner: ScheduledFuelSurchargeDraftStore,
    var cache: TtlCache<ScheduledFuelSurchargeDraft>,
    var hits: Int,
    var misses: Int,
) : ScheduledFuelSurchargeDraftStore {
    override fun find(code: String): ScheduledFuelSurchargeDraft? {
        val hit = (cache.get(code) as ScheduledFuelSurchargeDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledFuelSurchargeDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledFuelSurchargeDraft> = inner.list()
}

fun newCachedScheduledFuelSurchargeDraftStore(inner: ScheduledFuelSurchargeDraftStore, ttl: Int, clock: Clock): ScheduledFuelSurchargeDraftStore {
    return CachedScheduledFuelSurchargeDraftStore(
        inner = inner,
        cache = newTtlCache<ScheduledFuelSurchargeDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled fuel surcharge drafts. Empty fields match all. */
data class ScheduledFuelSurchargeDraftQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledFuelSurchargeDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledFuelSurchargeDraft>): MutableList<ScheduledFuelSurchargeDraft> {
        val matched = filter(items) { candidate: ScheduledFuelSurchargeDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledFuelSurchargeDrafts(store: ScheduledFuelSurchargeDraftStore, query: ScheduledFuelSurchargeDraftQuery): MutableList<ScheduledFuelSurchargeDraft> {
    return query.apply(store.list())
}
