package com.acme.transport.customs.hold.variance

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal hold audits by code. */
interface SeasonalHoldAuditStore {
    fun find(code: String): SeasonalHoldAudit?
    fun save(item: SeasonalHoldAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalHoldAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalHoldAuditStore(
    var items: MutableMap<String, SeasonalHoldAudit>,
    var order: MutableList<String>,
) : SeasonalHoldAuditStore {
    override fun find(code: String): SeasonalHoldAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalHoldAudit) {
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

    override fun list(): MutableList<SeasonalHoldAudit> {
        val out = mutableListOf<SeasonalHoldAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalHoldAuditStore(): SeasonalHoldAuditStore {
    return MemorySeasonalHoldAuditStore(mutableMapOf<String, SeasonalHoldAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalHoldAuditStore(
    var inner: SeasonalHoldAuditStore,
    var cache: TtlCache<SeasonalHoldAudit>,
    var hits: Int,
    var misses: Int,
) : SeasonalHoldAuditStore {
    override fun find(code: String): SeasonalHoldAudit? {
        val hit = (cache.get(code) as SeasonalHoldAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalHoldAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalHoldAudit> = inner.list()
}

fun newCachedSeasonalHoldAuditStore(inner: SeasonalHoldAuditStore, ttl: Int, clock: Clock): SeasonalHoldAuditStore {
    return CachedSeasonalHoldAuditStore(
        inner = inner,
        cache = newTtlCache<SeasonalHoldAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal hold audits. Empty fields match all. */
data class SeasonalHoldAuditQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalHoldAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<SeasonalHoldAudit>): MutableList<SeasonalHoldAudit> {
        val matched = filter(items) { candidate: SeasonalHoldAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalHoldAudits(store: SeasonalHoldAuditStore, query: SeasonalHoldAuditQuery): MutableList<SeasonalHoldAudit> {
    return query.apply(store.list())
}
