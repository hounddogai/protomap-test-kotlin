package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability drafts by code. */
interface AvailabilityDraftStore {
    fun find(code: String): AvailabilityDraft?
    fun save(item: AvailabilityDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityDraftStore(
    var items: MutableMap<String, AvailabilityDraft>,
    var order: MutableList<String>,
) : AvailabilityDraftStore {
    override fun find(code: String): AvailabilityDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityDraft) {
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

    override fun list(): MutableList<AvailabilityDraft> {
        val out = mutableListOf<AvailabilityDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityDraftStore(): AvailabilityDraftStore {
    return MemoryAvailabilityDraftStore(mutableMapOf<String, AvailabilityDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityDraftStore(
    var inner: AvailabilityDraftStore,
    var cache: TtlCache<AvailabilityDraft>,
    var hits: Int,
    var misses: Int,
) : AvailabilityDraftStore {
    override fun find(code: String): AvailabilityDraft? {
        val hit = (cache.get(code) as AvailabilityDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityDraft> = inner.list()
}

fun newCachedAvailabilityDraftStore(inner: AvailabilityDraftStore, ttl: Int, clock: Clock): AvailabilityDraftStore {
    return CachedAvailabilityDraftStore(
        inner = inner,
        cache = newTtlCache<AvailabilityDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability drafts. Empty fields match all. */
data class AvailabilityDraftQuery(
    var source: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityDraft): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityDraft>): MutableList<AvailabilityDraft> {
        val matched = filter(items) { candidate: AvailabilityDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityDrafts(store: AvailabilityDraftStore, query: AvailabilityDraftQuery): MutableList<AvailabilityDraft> {
    return query.apply(store.list())
}
