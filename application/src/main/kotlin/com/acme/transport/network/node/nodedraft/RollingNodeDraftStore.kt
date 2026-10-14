package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling node drafts by code. */
interface RollingNodeDraftStore {
    fun find(code: String): RollingNodeDraft?
    fun save(item: RollingNodeDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingNodeDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingNodeDraftStore(
    var items: MutableMap<String, RollingNodeDraft>,
    var order: MutableList<String>,
) : RollingNodeDraftStore {
    override fun find(code: String): RollingNodeDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingNodeDraft) {
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

    override fun list(): MutableList<RollingNodeDraft> {
        val out = mutableListOf<RollingNodeDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingNodeDraftStore(): RollingNodeDraftStore {
    return MemoryRollingNodeDraftStore(mutableMapOf<String, RollingNodeDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingNodeDraftStore(
    var inner: RollingNodeDraftStore,
    var cache: TtlCache<RollingNodeDraft>,
    var hits: Int,
    var misses: Int,
) : RollingNodeDraftStore {
    override fun find(code: String): RollingNodeDraft? {
        val hit = (cache.get(code) as RollingNodeDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingNodeDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingNodeDraft> = inner.list()
}

fun newCachedRollingNodeDraftStore(inner: RollingNodeDraftStore, ttl: Int, clock: Clock): RollingNodeDraftStore {
    return CachedRollingNodeDraftStore(
        inner = inner,
        cache = newTtlCache<RollingNodeDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling node drafts. Empty fields match all. */
data class RollingNodeDraftQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingNodeDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingNodeDraft>): MutableList<RollingNodeDraft> {
        val matched = filter(items) { candidate: RollingNodeDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingNodeDrafts(store: RollingNodeDraftStore, query: RollingNodeDraftQuery): MutableList<RollingNodeDraft> {
    return query.apply(store.list())
}
