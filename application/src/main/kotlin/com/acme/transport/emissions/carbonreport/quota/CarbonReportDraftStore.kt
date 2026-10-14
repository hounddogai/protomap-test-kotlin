package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report drafts by code. */
interface CarbonReportDraftStore {
    fun find(code: String): CarbonReportDraft?
    fun save(item: CarbonReportDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportDraftStore(
    var items: MutableMap<String, CarbonReportDraft>,
    var order: MutableList<String>,
) : CarbonReportDraftStore {
    override fun find(code: String): CarbonReportDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportDraft) {
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

    override fun list(): MutableList<CarbonReportDraft> {
        val out = mutableListOf<CarbonReportDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportDraftStore(): CarbonReportDraftStore {
    return MemoryCarbonReportDraftStore(mutableMapOf<String, CarbonReportDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportDraftStore(
    var inner: CarbonReportDraftStore,
    var cache: TtlCache<CarbonReportDraft>,
    var hits: Int,
    var misses: Int,
) : CarbonReportDraftStore {
    override fun find(code: String): CarbonReportDraft? {
        val hit = (cache.get(code) as CarbonReportDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportDraft> = inner.list()
}

fun newCachedCarbonReportDraftStore(inner: CarbonReportDraftStore, ttl: Int, clock: Clock): CarbonReportDraftStore {
    return CachedCarbonReportDraftStore(
        inner = inner,
        cache = newTtlCache<CarbonReportDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report drafts. Empty fields match all. */
data class CarbonReportDraftQuery(
    var depot: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportDraft): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportDraft>): MutableList<CarbonReportDraft> {
        val matched = filter(items) { candidate: CarbonReportDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportDrafts(store: CarbonReportDraftStore, query: CarbonReportDraftQuery): MutableList<CarbonReportDraft> {
    return query.apply(store.list())
}
