package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel drafts by code. */
interface ParcelDraftStore {
    fun find(code: String): ParcelDraft?
    fun save(item: ParcelDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelDraftStore(
    var items: MutableMap<String, ParcelDraft>,
    var order: MutableList<String>,
) : ParcelDraftStore {
    override fun find(code: String): ParcelDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelDraft) {
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

    override fun list(): MutableList<ParcelDraft> {
        val out = mutableListOf<ParcelDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelDraftStore(): ParcelDraftStore {
    return MemoryParcelDraftStore(mutableMapOf<String, ParcelDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelDraftStore(
    var inner: ParcelDraftStore,
    var cache: TtlCache<ParcelDraft>,
    var hits: Int,
    var misses: Int,
) : ParcelDraftStore {
    override fun find(code: String): ParcelDraft? {
        val hit = (cache.get(code) as ParcelDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelDraft> = inner.list()
}

fun newCachedParcelDraftStore(inner: ParcelDraftStore, ttl: Int, clock: Clock): ParcelDraftStore {
    return CachedParcelDraftStore(
        inner = inner,
        cache = newTtlCache<ParcelDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel drafts. Empty fields match all. */
data class ParcelDraftQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelDraft>): MutableList<ParcelDraft> {
        val matched = filter(items) { candidate: ParcelDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelDrafts(store: ParcelDraftStore, query: ParcelDraftQuery): MutableList<ParcelDraft> {
    return query.apply(store.list())
}
