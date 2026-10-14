package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy locker audits by code. */
interface LegacyLockerAuditStore {
    fun find(code: String): LegacyLockerAudit?
    fun save(item: LegacyLockerAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLockerAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLockerAuditStore(
    var items: MutableMap<String, LegacyLockerAudit>,
    var order: MutableList<String>,
) : LegacyLockerAuditStore {
    override fun find(code: String): LegacyLockerAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLockerAudit) {
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

    override fun list(): MutableList<LegacyLockerAudit> {
        val out = mutableListOf<LegacyLockerAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLockerAuditStore(): LegacyLockerAuditStore {
    return MemoryLegacyLockerAuditStore(mutableMapOf<String, LegacyLockerAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLockerAuditStore(
    var inner: LegacyLockerAuditStore,
    var cache: TtlCache<LegacyLockerAudit>,
    var hits: Int,
    var misses: Int,
) : LegacyLockerAuditStore {
    override fun find(code: String): LegacyLockerAudit? {
        val hit = (cache.get(code) as LegacyLockerAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLockerAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLockerAudit> = inner.list()
}

fun newCachedLegacyLockerAuditStore(inner: LegacyLockerAuditStore, ttl: Int, clock: Clock): LegacyLockerAuditStore {
    return CachedLegacyLockerAuditStore(
        inner = inner,
        cache = newTtlCache<LegacyLockerAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy locker audits. Empty fields match all. */
data class LegacyLockerAuditQuery(
    var category: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLockerAudit): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyLockerAudit>): MutableList<LegacyLockerAudit> {
        val matched = filter(items) { candidate: LegacyLockerAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLockerAudits(store: LegacyLockerAuditStore, query: LegacyLockerAuditQuery): MutableList<LegacyLockerAudit> {
    return query.apply(store.list())
}
