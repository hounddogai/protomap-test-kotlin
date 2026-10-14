package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit scores by code. */
interface AuditScoreStore {
    fun find(code: String): AuditScore?
    fun save(item: AuditScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditScoreStore(
    var items: MutableMap<String, AuditScore>,
    var order: MutableList<String>,
) : AuditScoreStore {
    override fun find(code: String): AuditScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditScore) {
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

    override fun list(): MutableList<AuditScore> {
        val out = mutableListOf<AuditScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditScoreStore(): AuditScoreStore {
    return MemoryAuditScoreStore(mutableMapOf<String, AuditScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditScoreStore(
    var inner: AuditScoreStore,
    var cache: TtlCache<AuditScore>,
    var hits: Int,
    var misses: Int,
) : AuditScoreStore {
    override fun find(code: String): AuditScore? {
        val hit = (cache.get(code) as AuditScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditScore> = inner.list()
}

fun newCachedAuditScoreStore(inner: AuditScoreStore, ttl: Int, clock: Clock): AuditScoreStore {
    return CachedAuditScoreStore(
        inner = inner,
        cache = newTtlCache<AuditScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit scores. Empty fields match all. */
data class AuditScoreQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<AuditScore>): MutableList<AuditScore> {
        val matched = filter(items) { candidate: AuditScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditScores(store: AuditScoreStore, query: AuditScoreQuery): MutableList<AuditScore> {
    return query.apply(store.list())
}
