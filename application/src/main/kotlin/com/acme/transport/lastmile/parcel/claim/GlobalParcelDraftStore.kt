package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global parcel drafts by code. */
interface GlobalParcelDraftStore {
    fun find(code: String): GlobalParcelDraft?
    fun save(item: GlobalParcelDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalParcelDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalParcelDraftStore(
    var items: MutableMap<String, GlobalParcelDraft>,
    var order: MutableList<String>,
) : GlobalParcelDraftStore {
    override fun find(code: String): GlobalParcelDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalParcelDraft) {
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

    override fun list(): MutableList<GlobalParcelDraft> {
        val out = mutableListOf<GlobalParcelDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalParcelDraftStore(): GlobalParcelDraftStore {
    return MemoryGlobalParcelDraftStore(mutableMapOf<String, GlobalParcelDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalParcelDraftStore(
    var inner: GlobalParcelDraftStore,
    var cache: TtlCache<GlobalParcelDraft>,
    var hits: Int,
    var misses: Int,
) : GlobalParcelDraftStore {
    override fun find(code: String): GlobalParcelDraft? {
        val hit = (cache.get(code) as GlobalParcelDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalParcelDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalParcelDraft> = inner.list()
}

fun newCachedGlobalParcelDraftStore(inner: GlobalParcelDraftStore, ttl: Int, clock: Clock): GlobalParcelDraftStore {
    return CachedGlobalParcelDraftStore(
        inner = inner,
        cache = newTtlCache<GlobalParcelDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global parcel drafts. Empty fields match all. */
data class GlobalParcelDraftQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalParcelDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalParcelDraft>): MutableList<GlobalParcelDraft> {
        val matched = filter(items) { candidate: GlobalParcelDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalParcelDrafts(store: GlobalParcelDraftStore, query: GlobalParcelDraftQuery): MutableList<GlobalParcelDraft> {
    return query.apply(store.list())
}
