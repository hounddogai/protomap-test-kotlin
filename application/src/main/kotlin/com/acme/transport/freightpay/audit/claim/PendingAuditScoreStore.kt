package com.acme.transport.freightpay.audit.claim

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending audit scores by code. */
interface PendingAuditScoreStore {
    fun find(code: String): PendingAuditScore?
    fun save(item: PendingAuditScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAuditScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAuditScoreStore(
    var items: MutableMap<String, PendingAuditScore>,
    var order: MutableList<String>,
) : PendingAuditScoreStore {
    override fun find(code: String): PendingAuditScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAuditScore) {
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

    override fun list(): MutableList<PendingAuditScore> {
        val out = mutableListOf<PendingAuditScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAuditScoreStore(): PendingAuditScoreStore {
    return MemoryPendingAuditScoreStore(mutableMapOf<String, PendingAuditScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAuditScoreStore(
    var inner: PendingAuditScoreStore,
    var cache: TtlCache<PendingAuditScore>,
    var hits: Int,
    var misses: Int,
) : PendingAuditScoreStore {
    override fun find(code: String): PendingAuditScore? {
        val hit = (cache.get(code) as PendingAuditScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAuditScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAuditScore> = inner.list()
}

fun newCachedPendingAuditScoreStore(inner: PendingAuditScoreStore, ttl: Int, clock: Clock): PendingAuditScoreStore {
    return CachedPendingAuditScoreStore(
        inner = inner,
        cache = newTtlCache<PendingAuditScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending audit scores. Empty fields match all. */
data class PendingAuditScoreQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAuditScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingAuditScore>): MutableList<PendingAuditScore> {
        val matched = filter(items) { candidate: PendingAuditScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAuditScores(store: PendingAuditScoreStore, query: PendingAuditScoreQuery): MutableList<PendingAuditScore> {
    return query.apply(store.list())
}
