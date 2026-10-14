package com.acme.transport.lastmile.drop.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop drafts by code. */
interface DropDraftStore {
    fun find(code: String): DropDraft?
    fun save(item: DropDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropDraftStore(
    var items: MutableMap<String, DropDraft>,
    var order: MutableList<String>,
) : DropDraftStore {
    override fun find(code: String): DropDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropDraft) {
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

    override fun list(): MutableList<DropDraft> {
        val out = mutableListOf<DropDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropDraftStore(): DropDraftStore {
    return MemoryDropDraftStore(mutableMapOf<String, DropDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropDraftStore(
    var inner: DropDraftStore,
    var cache: TtlCache<DropDraft>,
    var hits: Int,
    var misses: Int,
) : DropDraftStore {
    override fun find(code: String): DropDraft? {
        val hit = (cache.get(code) as DropDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropDraft> = inner.list()
}

fun newCachedDropDraftStore(inner: DropDraftStore, ttl: Int, clock: Clock): DropDraftStore {
    return CachedDropDraftStore(
        inner = inner,
        cache = newTtlCache<DropDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop drafts. Empty fields match all. */
data class DropDraftQuery(
    var depot: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropDraft): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropDraft>): MutableList<DropDraft> {
        val matched = filter(items) { candidate: DropDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropDrafts(store: DropDraftStore, query: DropDraftQuery): MutableList<DropDraft> {
    return query.apply(store.list())
}
