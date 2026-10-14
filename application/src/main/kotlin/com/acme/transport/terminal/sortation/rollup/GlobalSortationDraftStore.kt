package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global sortation drafts by code. */
interface GlobalSortationDraftStore {
    fun find(code: String): GlobalSortationDraft?
    fun save(item: GlobalSortationDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalSortationDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalSortationDraftStore(
    var items: MutableMap<String, GlobalSortationDraft>,
    var order: MutableList<String>,
) : GlobalSortationDraftStore {
    override fun find(code: String): GlobalSortationDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalSortationDraft) {
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

    override fun list(): MutableList<GlobalSortationDraft> {
        val out = mutableListOf<GlobalSortationDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalSortationDraftStore(): GlobalSortationDraftStore {
    return MemoryGlobalSortationDraftStore(
        mutableMapOf<String, GlobalSortationDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalSortationDraftStore(
    var inner: GlobalSortationDraftStore,
    var cache: TtlCache<GlobalSortationDraft>,
    var hits: Int,
    var misses: Int,
) : GlobalSortationDraftStore {
    override fun find(code: String): GlobalSortationDraft? {
        val hit = (cache.get(code) as GlobalSortationDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalSortationDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalSortationDraft> = inner.list()
}

fun newCachedGlobalSortationDraftStore(inner: GlobalSortationDraftStore, ttl: Int, clock: Clock): GlobalSortationDraftStore {
    return CachedGlobalSortationDraftStore(
        inner = inner,
        cache = newTtlCache<GlobalSortationDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global sortation drafts. Empty fields match all. */
data class GlobalSortationDraftQuery(
    var segment: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalSortationDraft): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalSortationDraft>): MutableList<GlobalSortationDraft> {
        val matched = filter(items) { candidate: GlobalSortationDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalSortationDrafts(store: GlobalSortationDraftStore, query: GlobalSortationDraftQuery): MutableList<GlobalSortationDraft> {
    return query.apply(store.list())
}
