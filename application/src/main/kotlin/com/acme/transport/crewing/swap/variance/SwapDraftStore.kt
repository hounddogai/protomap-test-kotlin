package com.acme.transport.crewing.swap.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap drafts by code. */
interface SwapDraftStore {
    fun find(code: String): SwapDraft?
    fun save(item: SwapDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapDraftStore(
    var items: MutableMap<String, SwapDraft>,
    var order: MutableList<String>,
) : SwapDraftStore {
    override fun find(code: String): SwapDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapDraft) {
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

    override fun list(): MutableList<SwapDraft> {
        val out = mutableListOf<SwapDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapDraftStore(): SwapDraftStore {
    return MemorySwapDraftStore(mutableMapOf<String, SwapDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapDraftStore(
    var inner: SwapDraftStore,
    var cache: TtlCache<SwapDraft>,
    var hits: Int,
    var misses: Int,
) : SwapDraftStore {
    override fun find(code: String): SwapDraft? {
        val hit = (cache.get(code) as SwapDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapDraft> = inner.list()
}

fun newCachedSwapDraftStore(inner: SwapDraftStore, ttl: Int, clock: Clock): SwapDraftStore {
    return CachedSwapDraftStore(
        inner = inner,
        cache = newTtlCache<SwapDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap drafts. Empty fields match all. */
data class SwapDraftQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<SwapDraft>): MutableList<SwapDraft> {
        val matched = filter(items) { candidate: SwapDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapDrafts(store: SwapDraftStore, query: SwapDraftQuery): MutableList<SwapDraft> {
    return query.apply(store.list())
}
