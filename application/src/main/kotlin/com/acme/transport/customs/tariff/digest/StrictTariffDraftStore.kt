package com.acme.transport.customs.tariff.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict tariff drafts by code. */
interface StrictTariffDraftStore {
    fun find(code: String): StrictTariffDraft?
    fun save(item: StrictTariffDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTariffDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTariffDraftStore(
    var items: MutableMap<String, StrictTariffDraft>,
    var order: MutableList<String>,
) : StrictTariffDraftStore {
    override fun find(code: String): StrictTariffDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTariffDraft) {
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

    override fun list(): MutableList<StrictTariffDraft> {
        val out = mutableListOf<StrictTariffDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTariffDraftStore(): StrictTariffDraftStore {
    return MemoryStrictTariffDraftStore(mutableMapOf<String, StrictTariffDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTariffDraftStore(
    var inner: StrictTariffDraftStore,
    var cache: TtlCache<StrictTariffDraft>,
    var hits: Int,
    var misses: Int,
) : StrictTariffDraftStore {
    override fun find(code: String): StrictTariffDraft? {
        val hit = (cache.get(code) as StrictTariffDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTariffDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTariffDraft> = inner.list()
}

fun newCachedStrictTariffDraftStore(inner: StrictTariffDraftStore, ttl: Int, clock: Clock): StrictTariffDraftStore {
    return CachedStrictTariffDraftStore(
        inner = inner,
        cache = newTtlCache<StrictTariffDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict tariff drafts. Empty fields match all. */
data class StrictTariffDraftQuery(
    var region: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTariffDraft): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTariffDraft>): MutableList<StrictTariffDraft> {
        val matched = filter(items) { candidate: StrictTariffDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTariffDrafts(store: StrictTariffDraftStore, query: StrictTariffDraftQuery): MutableList<StrictTariffDraft> {
    return query.apply(store.list())
}
