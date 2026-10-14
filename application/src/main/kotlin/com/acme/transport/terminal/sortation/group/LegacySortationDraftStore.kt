package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy sortation drafts by code. */
interface LegacySortationDraftStore {
    fun find(code: String): LegacySortationDraft?
    fun save(item: LegacySortationDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacySortationDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacySortationDraftStore(
    var items: MutableMap<String, LegacySortationDraft>,
    var order: MutableList<String>,
) : LegacySortationDraftStore {
    override fun find(code: String): LegacySortationDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacySortationDraft) {
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

    override fun list(): MutableList<LegacySortationDraft> {
        val out = mutableListOf<LegacySortationDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacySortationDraftStore(): LegacySortationDraftStore {
    return MemoryLegacySortationDraftStore(
        mutableMapOf<String, LegacySortationDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacySortationDraftStore(
    var inner: LegacySortationDraftStore,
    var cache: TtlCache<LegacySortationDraft>,
    var hits: Int,
    var misses: Int,
) : LegacySortationDraftStore {
    override fun find(code: String): LegacySortationDraft? {
        val hit = (cache.get(code) as LegacySortationDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacySortationDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacySortationDraft> = inner.list()
}

fun newCachedLegacySortationDraftStore(inner: LegacySortationDraftStore, ttl: Int, clock: Clock): LegacySortationDraftStore {
    return CachedLegacySortationDraftStore(
        inner = inner,
        cache = newTtlCache<LegacySortationDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy sortation drafts. Empty fields match all. */
data class LegacySortationDraftQuery(
    var country: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacySortationDraft): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<LegacySortationDraft>): MutableList<LegacySortationDraft> {
        val matched = filter(items) { candidate: LegacySortationDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacySortationDrafts(store: LegacySortationDraftStore, query: LegacySortationDraftQuery): MutableList<LegacySortationDraft> {
    return query.apply(store.list())
}
