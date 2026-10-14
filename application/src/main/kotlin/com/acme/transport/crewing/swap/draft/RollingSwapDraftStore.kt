package com.acme.transport.crewing.swap.draft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling swap drafts by code. */
interface RollingSwapDraftStore {
    fun find(code: String): RollingSwapDraft?
    fun save(item: RollingSwapDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingSwapDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingSwapDraftStore(
    var items: MutableMap<String, RollingSwapDraft>,
    var order: MutableList<String>,
) : RollingSwapDraftStore {
    override fun find(code: String): RollingSwapDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingSwapDraft) {
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

    override fun list(): MutableList<RollingSwapDraft> {
        val out = mutableListOf<RollingSwapDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingSwapDraftStore(): RollingSwapDraftStore {
    return MemoryRollingSwapDraftStore(mutableMapOf<String, RollingSwapDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingSwapDraftStore(
    var inner: RollingSwapDraftStore,
    var cache: TtlCache<RollingSwapDraft>,
    var hits: Int,
    var misses: Int,
) : RollingSwapDraftStore {
    override fun find(code: String): RollingSwapDraft? {
        val hit = (cache.get(code) as RollingSwapDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingSwapDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingSwapDraft> = inner.list()
}

fun newCachedRollingSwapDraftStore(inner: RollingSwapDraftStore, ttl: Int, clock: Clock): RollingSwapDraftStore {
    return CachedRollingSwapDraftStore(
        inner = inner,
        cache = newTtlCache<RollingSwapDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling swap drafts. Empty fields match all. */
data class RollingSwapDraftQuery(
    var category: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingSwapDraft): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingSwapDraft>): MutableList<RollingSwapDraft> {
        val matched = filter(items) { candidate: RollingSwapDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingSwapDrafts(store: RollingSwapDraftStore, query: RollingSwapDraftQuery): MutableList<RollingSwapDraft> {
    return query.apply(store.list())
}
