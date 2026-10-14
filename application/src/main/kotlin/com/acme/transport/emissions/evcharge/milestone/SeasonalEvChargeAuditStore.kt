package com.acme.transport.emissions.evcharge.milestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal ev charge audits by code. */
interface SeasonalEvChargeAuditStore {
    fun find(code: String): SeasonalEvChargeAudit?
    fun save(item: SeasonalEvChargeAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalEvChargeAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalEvChargeAuditStore(
    var items: MutableMap<String, SeasonalEvChargeAudit>,
    var order: MutableList<String>,
) : SeasonalEvChargeAuditStore {
    override fun find(code: String): SeasonalEvChargeAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalEvChargeAudit) {
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

    override fun list(): MutableList<SeasonalEvChargeAudit> {
        val out = mutableListOf<SeasonalEvChargeAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalEvChargeAuditStore(): SeasonalEvChargeAuditStore {
    return MemorySeasonalEvChargeAuditStore(
        mutableMapOf<String, SeasonalEvChargeAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalEvChargeAuditStore(
    var inner: SeasonalEvChargeAuditStore,
    var cache: TtlCache<SeasonalEvChargeAudit>,
    var hits: Int,
    var misses: Int,
) : SeasonalEvChargeAuditStore {
    override fun find(code: String): SeasonalEvChargeAudit? {
        val hit = (cache.get(code) as SeasonalEvChargeAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalEvChargeAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalEvChargeAudit> = inner.list()
}

fun newCachedSeasonalEvChargeAuditStore(inner: SeasonalEvChargeAuditStore, ttl: Int, clock: Clock): SeasonalEvChargeAuditStore {
    return CachedSeasonalEvChargeAuditStore(
        inner = inner,
        cache = newTtlCache<SeasonalEvChargeAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal ev charge audits. Empty fields match all. */
data class SeasonalEvChargeAuditQuery(
    var category: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalEvChargeAudit): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalEvChargeAudit>): MutableList<SeasonalEvChargeAudit> {
        val matched = filter(items) { candidate: SeasonalEvChargeAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalEvChargeAudits(store: SeasonalEvChargeAuditStore, query: SeasonalEvChargeAuditQuery): MutableList<SeasonalEvChargeAudit> {
    return query.apply(store.list())
}
