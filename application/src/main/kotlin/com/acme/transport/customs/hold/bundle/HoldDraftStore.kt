package com.acme.transport.customs.hold.bundle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold drafts by code. */
interface HoldDraftStore {
    fun find(code: String): HoldDraft?
    fun save(item: HoldDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldDraftStore(
    var items: MutableMap<String, HoldDraft>,
    var order: MutableList<String>,
) : HoldDraftStore {
    override fun find(code: String): HoldDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldDraft) {
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

    override fun list(): MutableList<HoldDraft> {
        val out = mutableListOf<HoldDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldDraftStore(): HoldDraftStore {
    return MemoryHoldDraftStore(mutableMapOf<String, HoldDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldDraftStore(
    var inner: HoldDraftStore,
    var cache: TtlCache<HoldDraft>,
    var hits: Int,
    var misses: Int,
) : HoldDraftStore {
    override fun find(code: String): HoldDraft? {
        val hit = (cache.get(code) as HoldDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldDraft> = inner.list()
}

fun newCachedHoldDraftStore(inner: HoldDraftStore, ttl: Int, clock: Clock): HoldDraftStore {
    return CachedHoldDraftStore(
        inner = inner,
        cache = newTtlCache<HoldDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold drafts. Empty fields match all. */
data class HoldDraftQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<HoldDraft>): MutableList<HoldDraft> {
        val matched = filter(items) { candidate: HoldDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldDrafts(store: HoldDraftStore, query: HoldDraftQuery): MutableList<HoldDraft> {
    return query.apply(store.list())
}
