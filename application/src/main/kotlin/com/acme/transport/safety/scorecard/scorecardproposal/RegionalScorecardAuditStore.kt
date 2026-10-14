package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional scorecard audits by code. */
interface RegionalScorecardAuditStore {
    fun find(code: String): RegionalScorecardAudit?
    fun save(item: RegionalScorecardAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalScorecardAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalScorecardAuditStore(
    var items: MutableMap<String, RegionalScorecardAudit>,
    var order: MutableList<String>,
) : RegionalScorecardAuditStore {
    override fun find(code: String): RegionalScorecardAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalScorecardAudit) {
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

    override fun list(): MutableList<RegionalScorecardAudit> {
        val out = mutableListOf<RegionalScorecardAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalScorecardAuditStore(): RegionalScorecardAuditStore {
    return MemoryRegionalScorecardAuditStore(
        mutableMapOf<String, RegionalScorecardAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalScorecardAuditStore(
    var inner: RegionalScorecardAuditStore,
    var cache: TtlCache<RegionalScorecardAudit>,
    var hits: Int,
    var misses: Int,
) : RegionalScorecardAuditStore {
    override fun find(code: String): RegionalScorecardAudit? {
        val hit = (cache.get(code) as RegionalScorecardAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalScorecardAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalScorecardAudit> = inner.list()
}

fun newCachedRegionalScorecardAuditStore(inner: RegionalScorecardAuditStore, ttl: Int, clock: Clock): RegionalScorecardAuditStore {
    return CachedRegionalScorecardAuditStore(
        inner = inner,
        cache = newTtlCache<RegionalScorecardAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional scorecard audits. Empty fields match all. */
data class RegionalScorecardAuditQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalScorecardAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalScorecardAudit>): MutableList<RegionalScorecardAudit> {
        val matched = filter(items) { candidate: RegionalScorecardAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalScorecardAudits(store: RegionalScorecardAuditStore, query: RegionalScorecardAuditQuery): MutableList<RegionalScorecardAudit> {
    return query.apply(store.list())
}
