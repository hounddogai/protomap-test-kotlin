package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual cutoff drafts by code. */
interface ManualCutoffDraftStore {
    fun find(code: String): ManualCutoffDraft?
    fun save(item: ManualCutoffDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCutoffDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCutoffDraftStore(
    var items: MutableMap<String, ManualCutoffDraft>,
    var order: MutableList<String>,
) : ManualCutoffDraftStore {
    override fun find(code: String): ManualCutoffDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCutoffDraft) {
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

    override fun list(): MutableList<ManualCutoffDraft> {
        val out = mutableListOf<ManualCutoffDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCutoffDraftStore(): ManualCutoffDraftStore {
    return MemoryManualCutoffDraftStore(mutableMapOf<String, ManualCutoffDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCutoffDraftStore(
    var inner: ManualCutoffDraftStore,
    var cache: TtlCache<ManualCutoffDraft>,
    var hits: Int,
    var misses: Int,
) : ManualCutoffDraftStore {
    override fun find(code: String): ManualCutoffDraft? {
        val hit = (cache.get(code) as ManualCutoffDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCutoffDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCutoffDraft> = inner.list()
}

fun newCachedManualCutoffDraftStore(inner: ManualCutoffDraftStore, ttl: Int, clock: Clock): ManualCutoffDraftStore {
    return CachedManualCutoffDraftStore(
        inner = inner,
        cache = newTtlCache<ManualCutoffDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual cutoff drafts. Empty fields match all. */
data class ManualCutoffDraftQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCutoffDraft): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<ManualCutoffDraft>): MutableList<ManualCutoffDraft> {
        val matched = filter(items) { candidate: ManualCutoffDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCutoffDrafts(store: ManualCutoffDraftStore, query: ManualCutoffDraftQuery): MutableList<ManualCutoffDraft> {
    return query.apply(store.list())
}
