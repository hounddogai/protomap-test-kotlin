package com.acme.transport.freightpay.ratecard.draft

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card drafts by code. */
interface RateCardDraftStore {
    fun find(code: String): RateCardDraft?
    fun save(item: RateCardDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardDraftStore(
    var items: MutableMap<String, RateCardDraft>,
    var order: MutableList<String>,
) : RateCardDraftStore {
    override fun find(code: String): RateCardDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardDraft) {
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

    override fun list(): MutableList<RateCardDraft> {
        val out = mutableListOf<RateCardDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardDraftStore(): RateCardDraftStore {
    return MemoryRateCardDraftStore(mutableMapOf<String, RateCardDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardDraftStore(
    var inner: RateCardDraftStore,
    var cache: TtlCache<RateCardDraft>,
    var hits: Int,
    var misses: Int,
) : RateCardDraftStore {
    override fun find(code: String): RateCardDraft? {
        val hit = (cache.get(code) as RateCardDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardDraft> = inner.list()
}

fun newCachedRateCardDraftStore(inner: RateCardDraftStore, ttl: Int, clock: Clock): RateCardDraftStore {
    return CachedRateCardDraftStore(
        inner = inner,
        cache = newTtlCache<RateCardDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card drafts. Empty fields match all. */
data class RateCardDraftQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardDraft): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardDraft>): MutableList<RateCardDraft> {
        val matched = filter(items) { candidate: RateCardDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardDrafts(store: RateCardDraftStore, query: RateCardDraftQuery): MutableList<RateCardDraft> {
    return query.apply(store.list())
}
