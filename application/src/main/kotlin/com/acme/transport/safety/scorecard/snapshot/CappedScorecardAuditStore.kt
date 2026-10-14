package com.acme.transport.safety.scorecard.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped scorecard audits by code. */
interface CappedScorecardAuditStore {
    fun find(code: String): CappedScorecardAudit?
    fun save(item: CappedScorecardAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedScorecardAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedScorecardAuditStore(
    var items: MutableMap<String, CappedScorecardAudit>,
    var order: MutableList<String>,
) : CappedScorecardAuditStore {
    override fun find(code: String): CappedScorecardAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedScorecardAudit) {
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

    override fun list(): MutableList<CappedScorecardAudit> {
        val out = mutableListOf<CappedScorecardAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedScorecardAuditStore(): CappedScorecardAuditStore {
    return MemoryCappedScorecardAuditStore(
        mutableMapOf<String, CappedScorecardAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedScorecardAuditStore(
    var inner: CappedScorecardAuditStore,
    var cache: TtlCache<CappedScorecardAudit>,
    var hits: Int,
    var misses: Int,
) : CappedScorecardAuditStore {
    override fun find(code: String): CappedScorecardAudit? {
        val hit = (cache.get(code) as CappedScorecardAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedScorecardAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedScorecardAudit> = inner.list()
}

fun newCachedCappedScorecardAuditStore(inner: CappedScorecardAuditStore, ttl: Int, clock: Clock): CappedScorecardAuditStore {
    return CachedCappedScorecardAuditStore(
        inner = inner,
        cache = newTtlCache<CappedScorecardAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped scorecard audits. Empty fields match all. */
data class CappedScorecardAuditQuery(
    var source: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedScorecardAudit): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedScorecardAudit>): MutableList<CappedScorecardAudit> {
        val matched = filter(items) { candidate: CappedScorecardAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedScorecardAudits(store: CappedScorecardAuditStore, query: CappedScorecardAuditQuery): MutableList<CappedScorecardAudit> {
    return query.apply(store.list())
}
