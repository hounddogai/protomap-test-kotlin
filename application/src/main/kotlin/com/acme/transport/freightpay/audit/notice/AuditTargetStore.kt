package com.acme.transport.freightpay.audit.notice

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit targets by code. */
interface AuditTargetStore {
    fun find(code: String): AuditTarget?
    fun save(item: AuditTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditTargetStore(
    var items: MutableMap<String, AuditTarget>,
    var order: MutableList<String>,
) : AuditTargetStore {
    override fun find(code: String): AuditTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditTarget) {
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

    override fun list(): MutableList<AuditTarget> {
        val out = mutableListOf<AuditTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditTargetStore(): AuditTargetStore {
    return MemoryAuditTargetStore(mutableMapOf<String, AuditTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditTargetStore(
    var inner: AuditTargetStore,
    var cache: TtlCache<AuditTarget>,
    var hits: Int,
    var misses: Int,
) : AuditTargetStore {
    override fun find(code: String): AuditTarget? {
        val hit = (cache.get(code) as AuditTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditTarget> = inner.list()
}

fun newCachedAuditTargetStore(inner: AuditTargetStore, ttl: Int, clock: Clock): AuditTargetStore {
    return CachedAuditTargetStore(
        inner = inner,
        cache = newTtlCache<AuditTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit targets. Empty fields match all. */
data class AuditTargetQuery(
    var depot: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditTarget>): MutableList<AuditTarget> {
        val matched = filter(items) { candidate: AuditTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditTargets(store: AuditTargetStore, query: AuditTargetQuery): MutableList<AuditTarget> {
    return query.apply(store.list())
}
