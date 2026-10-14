package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit cycles by code. */
interface AuditCycleStore {
    fun find(code: String): AuditCycle?
    fun save(item: AuditCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditCycleStore(
    var items: MutableMap<String, AuditCycle>,
    var order: MutableList<String>,
) : AuditCycleStore {
    override fun find(code: String): AuditCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditCycle) {
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

    override fun list(): MutableList<AuditCycle> {
        val out = mutableListOf<AuditCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditCycleStore(): AuditCycleStore {
    return MemoryAuditCycleStore(mutableMapOf<String, AuditCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditCycleStore(
    var inner: AuditCycleStore,
    var cache: TtlCache<AuditCycle>,
    var hits: Int,
    var misses: Int,
) : AuditCycleStore {
    override fun find(code: String): AuditCycle? {
        val hit = (cache.get(code) as AuditCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditCycle> = inner.list()
}

fun newCachedAuditCycleStore(inner: AuditCycleStore, ttl: Int, clock: Clock): AuditCycleStore {
    return CachedAuditCycleStore(
        inner = inner,
        cache = newTtlCache<AuditCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit cycles. Empty fields match all. */
data class AuditCycleQuery(
    var depot: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditCycle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<AuditCycle>): MutableList<AuditCycle> {
        val matched = filter(items) { candidate: AuditCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditCycles(store: AuditCycleStore, query: AuditCycleQuery): MutableList<AuditCycle> {
    return query.apply(store.list())
}
