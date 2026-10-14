package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary parcel drafts by code. */
interface PrimaryParcelDraftStore {
    fun find(code: String): PrimaryParcelDraft?
    fun save(item: PrimaryParcelDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryParcelDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryParcelDraftStore(
    var items: MutableMap<String, PrimaryParcelDraft>,
    var order: MutableList<String>,
) : PrimaryParcelDraftStore {
    override fun find(code: String): PrimaryParcelDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryParcelDraft) {
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

    override fun list(): MutableList<PrimaryParcelDraft> {
        val out = mutableListOf<PrimaryParcelDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryParcelDraftStore(): PrimaryParcelDraftStore {
    return MemoryPrimaryParcelDraftStore(mutableMapOf<String, PrimaryParcelDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryParcelDraftStore(
    var inner: PrimaryParcelDraftStore,
    var cache: TtlCache<PrimaryParcelDraft>,
    var hits: Int,
    var misses: Int,
) : PrimaryParcelDraftStore {
    override fun find(code: String): PrimaryParcelDraft? {
        val hit = (cache.get(code) as PrimaryParcelDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryParcelDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryParcelDraft> = inner.list()
}

fun newCachedPrimaryParcelDraftStore(inner: PrimaryParcelDraftStore, ttl: Int, clock: Clock): PrimaryParcelDraftStore {
    return CachedPrimaryParcelDraftStore(
        inner = inner,
        cache = newTtlCache<PrimaryParcelDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary parcel drafts. Empty fields match all. */
data class PrimaryParcelDraftQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryParcelDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryParcelDraft>): MutableList<PrimaryParcelDraft> {
        val matched = filter(items) { candidate: PrimaryParcelDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryParcelDrafts(store: PrimaryParcelDraftStore, query: PrimaryParcelDraftQuery): MutableList<PrimaryParcelDraft> {
    return query.apply(store.list())
}
