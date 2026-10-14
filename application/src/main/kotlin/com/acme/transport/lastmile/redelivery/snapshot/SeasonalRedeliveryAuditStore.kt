package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal redelivery audits by code. */
interface SeasonalRedeliveryAuditStore {
    fun find(code: String): SeasonalRedeliveryAudit?
    fun save(item: SeasonalRedeliveryAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRedeliveryAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRedeliveryAuditStore(
    var items: MutableMap<String, SeasonalRedeliveryAudit>,
    var order: MutableList<String>,
) : SeasonalRedeliveryAuditStore {
    override fun find(code: String): SeasonalRedeliveryAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRedeliveryAudit) {
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

    override fun list(): MutableList<SeasonalRedeliveryAudit> {
        val out = mutableListOf<SeasonalRedeliveryAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRedeliveryAuditStore(): SeasonalRedeliveryAuditStore {
    return MemorySeasonalRedeliveryAuditStore(
        mutableMapOf<String, SeasonalRedeliveryAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRedeliveryAuditStore(
    var inner: SeasonalRedeliveryAuditStore,
    var cache: TtlCache<SeasonalRedeliveryAudit>,
    var hits: Int,
    var misses: Int,
) : SeasonalRedeliveryAuditStore {
    override fun find(code: String): SeasonalRedeliveryAudit? {
        val hit = (cache.get(code) as SeasonalRedeliveryAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRedeliveryAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRedeliveryAudit> = inner.list()
}

fun newCachedSeasonalRedeliveryAuditStore(inner: SeasonalRedeliveryAuditStore, ttl: Int, clock: Clock): SeasonalRedeliveryAuditStore {
    return CachedSeasonalRedeliveryAuditStore(
        inner = inner,
        cache = newTtlCache<SeasonalRedeliveryAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal redelivery audits. Empty fields match all. */
data class SeasonalRedeliveryAuditQuery(
    var unit: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRedeliveryAudit): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRedeliveryAudit>): MutableList<SeasonalRedeliveryAudit> {
        val matched = filter(items) { candidate: SeasonalRedeliveryAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRedeliveryAudits(store: SeasonalRedeliveryAuditStore, query: SeasonalRedeliveryAuditQuery): MutableList<SeasonalRedeliveryAudit> {
    return query.apply(store.list())
}
