package com.acme.transport.network.hub.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub drafts by code. */
interface HubDraftStore {
    fun find(code: String): HubDraft?
    fun save(item: HubDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubDraftStore(
    var items: MutableMap<String, HubDraft>,
    var order: MutableList<String>,
) : HubDraftStore {
    override fun find(code: String): HubDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubDraft) {
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

    override fun list(): MutableList<HubDraft> {
        val out = mutableListOf<HubDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubDraftStore(): HubDraftStore {
    return MemoryHubDraftStore(mutableMapOf<String, HubDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubDraftStore(
    var inner: HubDraftStore,
    var cache: TtlCache<HubDraft>,
    var hits: Int,
    var misses: Int,
) : HubDraftStore {
    override fun find(code: String): HubDraft? {
        val hit = (cache.get(code) as HubDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubDraft> = inner.list()
}

fun newCachedHubDraftStore(inner: HubDraftStore, ttl: Int, clock: Clock): HubDraftStore {
    return CachedHubDraftStore(
        inner = inner,
        cache = newTtlCache<HubDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub drafts. Empty fields match all. */
data class HubDraftQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubDraft>): MutableList<HubDraft> {
        val matched = filter(items) { candidate: HubDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubDrafts(store: HubDraftStore, query: HubDraftQuery): MutableList<HubDraft> = query.apply(store.list())
