package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual trailer drafts by code. */
interface ManualTrailerDraftStore {
    fun find(code: String): ManualTrailerDraft?
    fun save(item: ManualTrailerDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTrailerDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTrailerDraftStore(
    var items: MutableMap<String, ManualTrailerDraft>,
    var order: MutableList<String>,
) : ManualTrailerDraftStore {
    override fun find(code: String): ManualTrailerDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTrailerDraft) {
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

    override fun list(): MutableList<ManualTrailerDraft> {
        val out = mutableListOf<ManualTrailerDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTrailerDraftStore(): ManualTrailerDraftStore {
    return MemoryManualTrailerDraftStore(mutableMapOf<String, ManualTrailerDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTrailerDraftStore(
    var inner: ManualTrailerDraftStore,
    var cache: TtlCache<ManualTrailerDraft>,
    var hits: Int,
    var misses: Int,
) : ManualTrailerDraftStore {
    override fun find(code: String): ManualTrailerDraft? {
        val hit = (cache.get(code) as ManualTrailerDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTrailerDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTrailerDraft> = inner.list()
}

fun newCachedManualTrailerDraftStore(inner: ManualTrailerDraftStore, ttl: Int, clock: Clock): ManualTrailerDraftStore {
    return CachedManualTrailerDraftStore(
        inner = inner,
        cache = newTtlCache<ManualTrailerDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual trailer drafts. Empty fields match all. */
data class ManualTrailerDraftQuery(
    var lane: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTrailerDraft): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<ManualTrailerDraft>): MutableList<ManualTrailerDraft> {
        val matched = filter(items) { candidate: ManualTrailerDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTrailerDrafts(store: ManualTrailerDraftStore, query: ManualTrailerDraftQuery): MutableList<ManualTrailerDraft> {
    return query.apply(store.list())
}
