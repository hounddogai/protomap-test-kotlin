package com.acme.transport.customs.tariff.cycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff audits by code. */
interface TariffAuditStore {
    fun find(code: String): TariffAudit?
    fun save(item: TariffAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffAuditStore(
    var items: MutableMap<String, TariffAudit>,
    var order: MutableList<String>,
) : TariffAuditStore {
    override fun find(code: String): TariffAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffAudit) {
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

    override fun list(): MutableList<TariffAudit> {
        val out = mutableListOf<TariffAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffAuditStore(): TariffAuditStore {
    return MemoryTariffAuditStore(mutableMapOf<String, TariffAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffAuditStore(
    var inner: TariffAuditStore,
    var cache: TtlCache<TariffAudit>,
    var hits: Int,
    var misses: Int,
) : TariffAuditStore {
    override fun find(code: String): TariffAudit? {
        val hit = (cache.get(code) as TariffAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffAudit> = inner.list()
}

fun newCachedTariffAuditStore(inner: TariffAuditStore, ttl: Int, clock: Clock): TariffAuditStore {
    return CachedTariffAuditStore(
        inner = inner,
        cache = newTtlCache<TariffAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff audits. Empty fields match all. */
data class TariffAuditQuery(
    var source: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffAudit): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffAudit>): MutableList<TariffAudit> {
        val matched = filter(items) { candidate: TariffAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffAudits(store: TariffAuditStore, query: TariffAuditQuery): MutableList<TariffAudit> {
    return query.apply(store.list())
}
