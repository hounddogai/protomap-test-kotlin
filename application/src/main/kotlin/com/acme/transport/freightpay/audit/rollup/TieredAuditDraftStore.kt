package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered audit drafts by code. */
interface TieredAuditDraftStore {
    fun find(code: String): TieredAuditDraft?
    fun save(item: TieredAuditDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredAuditDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredAuditDraftStore(
    var items: MutableMap<String, TieredAuditDraft>,
    var order: MutableList<String>,
) : TieredAuditDraftStore {
    override fun find(code: String): TieredAuditDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredAuditDraft) {
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

    override fun list(): MutableList<TieredAuditDraft> {
        val out = mutableListOf<TieredAuditDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredAuditDraftStore(): TieredAuditDraftStore {
    return MemoryTieredAuditDraftStore(mutableMapOf<String, TieredAuditDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredAuditDraftStore(
    var inner: TieredAuditDraftStore,
    var cache: TtlCache<TieredAuditDraft>,
    var hits: Int,
    var misses: Int,
) : TieredAuditDraftStore {
    override fun find(code: String): TieredAuditDraft? {
        val hit = (cache.get(code) as TieredAuditDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredAuditDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredAuditDraft> = inner.list()
}

fun newCachedTieredAuditDraftStore(inner: TieredAuditDraftStore, ttl: Int, clock: Clock): TieredAuditDraftStore {
    return CachedTieredAuditDraftStore(
        inner = inner,
        cache = newTtlCache<TieredAuditDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered audit drafts. Empty fields match all. */
data class TieredAuditDraftQuery(
    var region: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredAuditDraft): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredAuditDraft>): MutableList<TieredAuditDraft> {
        val matched = filter(items) { candidate: TieredAuditDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredAuditDrafts(store: TieredAuditDraftStore, query: TieredAuditDraftQuery): MutableList<TieredAuditDraft> {
    return query.apply(store.list())
}
