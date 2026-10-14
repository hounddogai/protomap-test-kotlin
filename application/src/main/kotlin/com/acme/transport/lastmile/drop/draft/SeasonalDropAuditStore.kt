package com.acme.transport.lastmile.drop.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal drop audits by code. */
interface SeasonalDropAuditStore {
    fun find(code: String): SeasonalDropAudit?
    fun save(item: SeasonalDropAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDropAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDropAuditStore(
    var items: MutableMap<String, SeasonalDropAudit>,
    var order: MutableList<String>,
) : SeasonalDropAuditStore {
    override fun find(code: String): SeasonalDropAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDropAudit) {
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

    override fun list(): MutableList<SeasonalDropAudit> {
        val out = mutableListOf<SeasonalDropAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDropAuditStore(): SeasonalDropAuditStore {
    return MemorySeasonalDropAuditStore(mutableMapOf<String, SeasonalDropAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDropAuditStore(
    var inner: SeasonalDropAuditStore,
    var cache: TtlCache<SeasonalDropAudit>,
    var hits: Int,
    var misses: Int,
) : SeasonalDropAuditStore {
    override fun find(code: String): SeasonalDropAudit? {
        val hit = (cache.get(code) as SeasonalDropAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDropAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDropAudit> = inner.list()
}

fun newCachedSeasonalDropAuditStore(inner: SeasonalDropAuditStore, ttl: Int, clock: Clock): SeasonalDropAuditStore {
    return CachedSeasonalDropAuditStore(
        inner = inner,
        cache = newTtlCache<SeasonalDropAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal drop audits. Empty fields match all. */
data class SeasonalDropAuditQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDropAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDropAudit>): MutableList<SeasonalDropAudit> {
        val matched = filter(items) { candidate: SeasonalDropAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDropAudits(store: SeasonalDropAuditStore, query: SeasonalDropAuditQuery): MutableList<SeasonalDropAudit> {
    return query.apply(store.list())
}
