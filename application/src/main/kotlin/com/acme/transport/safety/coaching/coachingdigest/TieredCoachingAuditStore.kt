package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered coaching audits by code. */
interface TieredCoachingAuditStore {
    fun find(code: String): TieredCoachingAudit?
    fun save(item: TieredCoachingAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCoachingAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCoachingAuditStore(
    var items: MutableMap<String, TieredCoachingAudit>,
    var order: MutableList<String>,
) : TieredCoachingAuditStore {
    override fun find(code: String): TieredCoachingAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCoachingAudit) {
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

    override fun list(): MutableList<TieredCoachingAudit> {
        val out = mutableListOf<TieredCoachingAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCoachingAuditStore(): TieredCoachingAuditStore {
    return MemoryTieredCoachingAuditStore(mutableMapOf<String, TieredCoachingAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCoachingAuditStore(
    var inner: TieredCoachingAuditStore,
    var cache: TtlCache<TieredCoachingAudit>,
    var hits: Int,
    var misses: Int,
) : TieredCoachingAuditStore {
    override fun find(code: String): TieredCoachingAudit? {
        val hit = (cache.get(code) as TieredCoachingAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCoachingAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCoachingAudit> = inner.list()
}

fun newCachedTieredCoachingAuditStore(inner: TieredCoachingAuditStore, ttl: Int, clock: Clock): TieredCoachingAuditStore {
    return CachedTieredCoachingAuditStore(
        inner = inner,
        cache = newTtlCache<TieredCoachingAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered coaching audits. Empty fields match all. */
data class TieredCoachingAuditQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCoachingAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCoachingAudit>): MutableList<TieredCoachingAudit> {
        val matched = filter(items) { candidate: TieredCoachingAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCoachingAudits(store: TieredCoachingAuditStore, query: TieredCoachingAuditQuery): MutableList<TieredCoachingAudit> {
    return query.apply(store.list())
}
