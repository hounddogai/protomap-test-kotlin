package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit thresholds by code. */
interface AuditThresholdStore {
    fun find(code: String): AuditThreshold?
    fun save(item: AuditThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditThresholdStore(
    var items: MutableMap<String, AuditThreshold>,
    var order: MutableList<String>,
) : AuditThresholdStore {
    override fun find(code: String): AuditThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditThreshold) {
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

    override fun list(): MutableList<AuditThreshold> {
        val out = mutableListOf<AuditThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditThresholdStore(): AuditThresholdStore {
    return MemoryAuditThresholdStore(mutableMapOf<String, AuditThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditThresholdStore(
    var inner: AuditThresholdStore,
    var cache: TtlCache<AuditThreshold>,
    var hits: Int,
    var misses: Int,
) : AuditThresholdStore {
    override fun find(code: String): AuditThreshold? {
        val hit = (cache.get(code) as AuditThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditThreshold> = inner.list()
}

fun newCachedAuditThresholdStore(inner: AuditThresholdStore, ttl: Int, clock: Clock): AuditThresholdStore {
    return CachedAuditThresholdStore(
        inner = inner,
        cache = newTtlCache<AuditThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit thresholds. Empty fields match all. */
data class AuditThresholdQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditThreshold>): MutableList<AuditThreshold> {
        val matched = filter(items) { candidate: AuditThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditThresholds(store: AuditThresholdStore, query: AuditThresholdQuery): MutableList<AuditThreshold> {
    return query.apply(store.list())
}
