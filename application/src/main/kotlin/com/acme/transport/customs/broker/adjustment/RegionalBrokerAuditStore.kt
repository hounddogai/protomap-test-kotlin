package com.acme.transport.customs.broker.adjustment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional broker audits by code. */
interface RegionalBrokerAuditStore {
    fun find(code: String): RegionalBrokerAudit?
    fun save(item: RegionalBrokerAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalBrokerAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalBrokerAuditStore(
    var items: MutableMap<String, RegionalBrokerAudit>,
    var order: MutableList<String>,
) : RegionalBrokerAuditStore {
    override fun find(code: String): RegionalBrokerAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalBrokerAudit) {
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

    override fun list(): MutableList<RegionalBrokerAudit> {
        val out = mutableListOf<RegionalBrokerAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalBrokerAuditStore(): RegionalBrokerAuditStore {
    return MemoryRegionalBrokerAuditStore(mutableMapOf<String, RegionalBrokerAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalBrokerAuditStore(
    var inner: RegionalBrokerAuditStore,
    var cache: TtlCache<RegionalBrokerAudit>,
    var hits: Int,
    var misses: Int,
) : RegionalBrokerAuditStore {
    override fun find(code: String): RegionalBrokerAudit? {
        val hit = (cache.get(code) as RegionalBrokerAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalBrokerAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalBrokerAudit> = inner.list()
}

fun newCachedRegionalBrokerAuditStore(inner: RegionalBrokerAuditStore, ttl: Int, clock: Clock): RegionalBrokerAuditStore {
    return CachedRegionalBrokerAuditStore(
        inner = inner,
        cache = newTtlCache<RegionalBrokerAudit>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional broker audits. Empty fields match all. */
data class RegionalBrokerAuditQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalBrokerAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalBrokerAudit>): MutableList<RegionalBrokerAudit> {
        val matched = filter(items) { candidate: RegionalBrokerAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalBrokerAudits(store: RegionalBrokerAuditStore, query: RegionalBrokerAuditQuery): MutableList<RegionalBrokerAudit> {
    return query.apply(store.list())
}
