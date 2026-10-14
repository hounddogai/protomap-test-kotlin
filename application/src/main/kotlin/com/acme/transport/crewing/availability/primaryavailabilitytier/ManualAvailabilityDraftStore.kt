package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual availability drafts by code. */
interface ManualAvailabilityDraftStore {
    fun find(code: String): ManualAvailabilityDraft?
    fun save(item: ManualAvailabilityDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAvailabilityDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAvailabilityDraftStore(
    var items: MutableMap<String, ManualAvailabilityDraft>,
    var order: MutableList<String>,
) : ManualAvailabilityDraftStore {
    override fun find(code: String): ManualAvailabilityDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAvailabilityDraft) {
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

    override fun list(): MutableList<ManualAvailabilityDraft> {
        val out = mutableListOf<ManualAvailabilityDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAvailabilityDraftStore(): ManualAvailabilityDraftStore {
    return MemoryManualAvailabilityDraftStore(
        mutableMapOf<String, ManualAvailabilityDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAvailabilityDraftStore(
    var inner: ManualAvailabilityDraftStore,
    var cache: TtlCache<ManualAvailabilityDraft>,
    var hits: Int,
    var misses: Int,
) : ManualAvailabilityDraftStore {
    override fun find(code: String): ManualAvailabilityDraft? {
        val hit = (cache.get(code) as ManualAvailabilityDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAvailabilityDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAvailabilityDraft> = inner.list()
}

fun newCachedManualAvailabilityDraftStore(inner: ManualAvailabilityDraftStore, ttl: Int, clock: Clock): ManualAvailabilityDraftStore {
    return CachedManualAvailabilityDraftStore(
        inner = inner,
        cache = newTtlCache<ManualAvailabilityDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual availability drafts. Empty fields match all. */
data class ManualAvailabilityDraftQuery(
    var locale: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAvailabilityDraft): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAvailabilityDraft>): MutableList<ManualAvailabilityDraft> {
        val matched = filter(items) { candidate: ManualAvailabilityDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAvailabilityDrafts(store: ManualAvailabilityDraftStore, query: ManualAvailabilityDraftQuery): MutableList<ManualAvailabilityDraft> {
    return query.apply(store.list())
}
