package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock drafts by code. */
interface CrossDockDraftStore {
    fun find(code: String): CrossDockDraft?
    fun save(item: CrossDockDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockDraftStore(
    var items: MutableMap<String, CrossDockDraft>,
    var order: MutableList<String>,
) : CrossDockDraftStore {
    override fun find(code: String): CrossDockDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockDraft) {
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

    override fun list(): MutableList<CrossDockDraft> {
        val out = mutableListOf<CrossDockDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockDraftStore(): CrossDockDraftStore {
    return MemoryCrossDockDraftStore(mutableMapOf<String, CrossDockDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockDraftStore(
    var inner: CrossDockDraftStore,
    var cache: TtlCache<CrossDockDraft>,
    var hits: Int,
    var misses: Int,
) : CrossDockDraftStore {
    override fun find(code: String): CrossDockDraft? {
        val hit = (cache.get(code) as CrossDockDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockDraft> = inner.list()
}

fun newCachedCrossDockDraftStore(inner: CrossDockDraftStore, ttl: Int, clock: Clock): CrossDockDraftStore {
    return CachedCrossDockDraftStore(
        inner = inner,
        cache = newTtlCache<CrossDockDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock drafts. Empty fields match all. */
data class CrossDockDraftQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockDraft>): MutableList<CrossDockDraft> {
        val matched = filter(items) { candidate: CrossDockDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockDrafts(store: CrossDockDraftStore, query: CrossDockDraftQuery): MutableList<CrossDockDraft> {
    return query.apply(store.list())
}
