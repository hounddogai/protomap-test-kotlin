package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit baselines by code. */
interface AuditBaselineStore {
    fun find(code: String): AuditBaseline?
    fun save(item: AuditBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditBaselineStore(
    var items: MutableMap<String, AuditBaseline>,
    var order: MutableList<String>,
) : AuditBaselineStore {
    override fun find(code: String): AuditBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditBaseline) {
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

    override fun list(): MutableList<AuditBaseline> {
        val out = mutableListOf<AuditBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditBaselineStore(): AuditBaselineStore {
    return MemoryAuditBaselineStore(mutableMapOf<String, AuditBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditBaselineStore(
    var inner: AuditBaselineStore,
    var cache: TtlCache<AuditBaseline>,
    var hits: Int,
    var misses: Int,
) : AuditBaselineStore {
    override fun find(code: String): AuditBaseline? {
        val hit = (cache.get(code) as AuditBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditBaseline> = inner.list()
}

fun newCachedAuditBaselineStore(inner: AuditBaselineStore, ttl: Int, clock: Clock): AuditBaselineStore {
    return CachedAuditBaselineStore(
        inner = inner,
        cache = newTtlCache<AuditBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit baselines. Empty fields match all. */
data class AuditBaselineQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<AuditBaseline>): MutableList<AuditBaseline> {
        val matched = filter(items) { candidate: AuditBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditBaselines(store: AuditBaselineStore, query: AuditBaselineQuery): MutableList<AuditBaseline> {
    return query.apply(store.list())
}
