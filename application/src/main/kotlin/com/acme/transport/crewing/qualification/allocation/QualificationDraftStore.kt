package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification drafts by code. */
interface QualificationDraftStore {
    fun find(code: String): QualificationDraft?
    fun save(item: QualificationDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationDraftStore(
    var items: MutableMap<String, QualificationDraft>,
    var order: MutableList<String>,
) : QualificationDraftStore {
    override fun find(code: String): QualificationDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationDraft) {
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

    override fun list(): MutableList<QualificationDraft> {
        val out = mutableListOf<QualificationDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationDraftStore(): QualificationDraftStore {
    return MemoryQualificationDraftStore(mutableMapOf<String, QualificationDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationDraftStore(
    var inner: QualificationDraftStore,
    var cache: TtlCache<QualificationDraft>,
    var hits: Int,
    var misses: Int,
) : QualificationDraftStore {
    override fun find(code: String): QualificationDraft? {
        val hit = (cache.get(code) as QualificationDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationDraft> = inner.list()
}

fun newCachedQualificationDraftStore(inner: QualificationDraftStore, ttl: Int, clock: Clock): QualificationDraftStore {
    return CachedQualificationDraftStore(
        inner = inner,
        cache = newTtlCache<QualificationDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification drafts. Empty fields match all. */
data class QualificationDraftQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationDraft>): MutableList<QualificationDraft> {
        val matched = filter(items) { candidate: QualificationDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationDrafts(store: QualificationDraftStore, query: QualificationDraftQuery): MutableList<QualificationDraft> {
    return query.apply(store.list())
}
