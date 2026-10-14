package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dwell drafts by code. */
interface CappedDwellDraftStore {
    fun find(code: String): CappedDwellDraft?
    fun save(item: CappedDwellDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDwellDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDwellDraftStore(
    var items: MutableMap<String, CappedDwellDraft>,
    var order: MutableList<String>,
) : CappedDwellDraftStore {
    override fun find(code: String): CappedDwellDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDwellDraft) {
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

    override fun list(): MutableList<CappedDwellDraft> {
        val out = mutableListOf<CappedDwellDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDwellDraftStore(): CappedDwellDraftStore {
    return MemoryCappedDwellDraftStore(mutableMapOf<String, CappedDwellDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDwellDraftStore(
    var inner: CappedDwellDraftStore,
    var cache: TtlCache<CappedDwellDraft>,
    var hits: Int,
    var misses: Int,
) : CappedDwellDraftStore {
    override fun find(code: String): CappedDwellDraft? {
        val hit = (cache.get(code) as CappedDwellDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDwellDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDwellDraft> = inner.list()
}

fun newCachedCappedDwellDraftStore(inner: CappedDwellDraftStore, ttl: Int, clock: Clock): CappedDwellDraftStore {
    return CachedCappedDwellDraftStore(
        inner = inner,
        cache = newTtlCache<CappedDwellDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dwell drafts. Empty fields match all. */
data class CappedDwellDraftQuery(
    var locale: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDwellDraft): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<CappedDwellDraft>): MutableList<CappedDwellDraft> {
        val matched = filter(items) { candidate: CappedDwellDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDwellDrafts(store: CappedDwellDraftStore, query: CappedDwellDraftQuery): MutableList<CappedDwellDraft> {
    return query.apply(store.list())
}
