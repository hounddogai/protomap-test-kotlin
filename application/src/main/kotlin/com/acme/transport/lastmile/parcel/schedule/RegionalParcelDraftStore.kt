package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional parcel drafts by code. */
interface RegionalParcelDraftStore {
    fun find(code: String): RegionalParcelDraft?
    fun save(item: RegionalParcelDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalParcelDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalParcelDraftStore(
    var items: MutableMap<String, RegionalParcelDraft>,
    var order: MutableList<String>,
) : RegionalParcelDraftStore {
    override fun find(code: String): RegionalParcelDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalParcelDraft) {
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

    override fun list(): MutableList<RegionalParcelDraft> {
        val out = mutableListOf<RegionalParcelDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalParcelDraftStore(): RegionalParcelDraftStore {
    return MemoryRegionalParcelDraftStore(mutableMapOf<String, RegionalParcelDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalParcelDraftStore(
    var inner: RegionalParcelDraftStore,
    var cache: TtlCache<RegionalParcelDraft>,
    var hits: Int,
    var misses: Int,
) : RegionalParcelDraftStore {
    override fun find(code: String): RegionalParcelDraft? {
        val hit = (cache.get(code) as RegionalParcelDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalParcelDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalParcelDraft> = inner.list()
}

fun newCachedRegionalParcelDraftStore(inner: RegionalParcelDraftStore, ttl: Int, clock: Clock): RegionalParcelDraftStore {
    return CachedRegionalParcelDraftStore(
        inner = inner,
        cache = newTtlCache<RegionalParcelDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional parcel drafts. Empty fields match all. */
data class RegionalParcelDraftQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalParcelDraft): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<RegionalParcelDraft>): MutableList<RegionalParcelDraft> {
        val matched = filter(items) { candidate: RegionalParcelDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalParcelDrafts(store: RegionalParcelDraftStore, query: RegionalParcelDraftQuery): MutableList<RegionalParcelDraft> {
    return query.apply(store.list())
}
