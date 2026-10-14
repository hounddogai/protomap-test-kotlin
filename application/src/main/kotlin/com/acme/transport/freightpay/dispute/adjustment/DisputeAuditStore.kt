package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute audits by code. */
interface DisputeAuditStore {
    fun find(code: String): DisputeAudit?
    fun save(item: DisputeAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeAuditStore(
    var items: MutableMap<String, DisputeAudit>,
    var order: MutableList<String>,
) : DisputeAuditStore {
    override fun find(code: String): DisputeAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeAudit) {
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

    override fun list(): MutableList<DisputeAudit> {
        val out = mutableListOf<DisputeAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeAuditStore(): DisputeAuditStore {
    return MemoryDisputeAuditStore(mutableMapOf<String, DisputeAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeAuditStore(
    var inner: DisputeAuditStore,
    var cache: TtlCache<DisputeAudit>,
    var hits: Int,
    var misses: Int,
) : DisputeAuditStore {
    override fun find(code: String): DisputeAudit? {
        val hit = (cache.get(code) as DisputeAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeAudit> = inner.list()
}

fun newCachedDisputeAuditStore(inner: DisputeAuditStore, ttl: Int, clock: Clock): DisputeAuditStore {
    return CachedDisputeAuditStore(
        inner = inner,
        cache = newTtlCache<DisputeAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute audits. Empty fields match all. */
data class DisputeAuditQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeAudit>): MutableList<DisputeAudit> {
        val matched = filter(items) { candidate: DisputeAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeAudits(store: DisputeAuditStore, query: DisputeAuditQuery): MutableList<DisputeAudit> {
    return query.apply(store.list())
}
