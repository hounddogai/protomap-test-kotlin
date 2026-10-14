package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered hold drafts by code. */
interface TieredHoldDraftStore {
    fun find(code: String): TieredHoldDraft?
    fun save(item: TieredHoldDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredHoldDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredHoldDraftStore(
    var items: MutableMap<String, TieredHoldDraft>,
    var order: MutableList<String>,
) : TieredHoldDraftStore {
    override fun find(code: String): TieredHoldDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredHoldDraft) {
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

    override fun list(): MutableList<TieredHoldDraft> {
        val out = mutableListOf<TieredHoldDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredHoldDraftStore(): TieredHoldDraftStore {
    return MemoryTieredHoldDraftStore(mutableMapOf<String, TieredHoldDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredHoldDraftStore(
    var inner: TieredHoldDraftStore,
    var cache: TtlCache<TieredHoldDraft>,
    var hits: Int,
    var misses: Int,
) : TieredHoldDraftStore {
    override fun find(code: String): TieredHoldDraft? {
        val hit = (cache.get(code) as TieredHoldDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredHoldDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredHoldDraft> = inner.list()
}

fun newCachedTieredHoldDraftStore(inner: TieredHoldDraftStore, ttl: Int, clock: Clock): TieredHoldDraftStore {
    return CachedTieredHoldDraftStore(
        inner = inner,
        cache = newTtlCache<TieredHoldDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered hold drafts. Empty fields match all. */
data class TieredHoldDraftQuery(
    var region: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredHoldDraft): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredHoldDraft>): MutableList<TieredHoldDraft> {
        val matched = filter(items) { candidate: TieredHoldDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredHoldDrafts(store: TieredHoldDraftStore, query: TieredHoldDraftQuery): MutableList<TieredHoldDraft> {
    return query.apply(store.list())
}
