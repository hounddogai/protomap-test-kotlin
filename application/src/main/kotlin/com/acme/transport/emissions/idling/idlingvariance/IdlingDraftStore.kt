package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling drafts by code. */
interface IdlingDraftStore {
    fun find(code: String): IdlingDraft?
    fun save(item: IdlingDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingDraftStore(
    var items: MutableMap<String, IdlingDraft>,
    var order: MutableList<String>,
) : IdlingDraftStore {
    override fun find(code: String): IdlingDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingDraft) {
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

    override fun list(): MutableList<IdlingDraft> {
        val out = mutableListOf<IdlingDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingDraftStore(): IdlingDraftStore {
    return MemoryIdlingDraftStore(mutableMapOf<String, IdlingDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingDraftStore(
    var inner: IdlingDraftStore,
    var cache: TtlCache<IdlingDraft>,
    var hits: Int,
    var misses: Int,
) : IdlingDraftStore {
    override fun find(code: String): IdlingDraft? {
        val hit = (cache.get(code) as IdlingDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingDraft> = inner.list()
}

fun newCachedIdlingDraftStore(inner: IdlingDraftStore, ttl: Int, clock: Clock): IdlingDraftStore {
    return CachedIdlingDraftStore(
        inner = inner,
        cache = newTtlCache<IdlingDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling drafts. Empty fields match all. */
data class IdlingDraftQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingDraft>): MutableList<IdlingDraft> {
        val matched = filter(items) { candidate: IdlingDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingDrafts(store: IdlingDraftStore, query: IdlingDraftQuery): MutableList<IdlingDraft> {
    return query.apply(store.list())
}
