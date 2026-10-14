package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default rate card audits by code. */
interface DefaultRateCardAuditStore {
    fun find(code: String): DefaultRateCardAudit?
    fun save(item: DefaultRateCardAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRateCardAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRateCardAuditStore(
    var items: MutableMap<String, DefaultRateCardAudit>,
    var order: MutableList<String>,
) : DefaultRateCardAuditStore {
    override fun find(code: String): DefaultRateCardAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRateCardAudit) {
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

    override fun list(): MutableList<DefaultRateCardAudit> {
        val out = mutableListOf<DefaultRateCardAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRateCardAuditStore(): DefaultRateCardAuditStore {
    return MemoryDefaultRateCardAuditStore(
        mutableMapOf<String, DefaultRateCardAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRateCardAuditStore(
    var inner: DefaultRateCardAuditStore,
    var cache: TtlCache<DefaultRateCardAudit>,
    var hits: Int,
    var misses: Int,
) : DefaultRateCardAuditStore {
    override fun find(code: String): DefaultRateCardAudit? {
        val hit = (cache.get(code) as DefaultRateCardAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRateCardAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRateCardAudit> = inner.list()
}

fun newCachedDefaultRateCardAuditStore(inner: DefaultRateCardAuditStore, ttl: Int, clock: Clock): DefaultRateCardAuditStore {
    return CachedDefaultRateCardAuditStore(
        inner = inner,
        cache = newTtlCache<DefaultRateCardAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default rate card audits. Empty fields match all. */
data class DefaultRateCardAuditQuery(
    var country: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRateCardAudit): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRateCardAudit>): MutableList<DefaultRateCardAudit> {
        val matched = filter(items) { candidate: DefaultRateCardAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRateCardAudits(store: DefaultRateCardAuditStore, query: DefaultRateCardAuditQuery): MutableList<DefaultRateCardAudit> {
    return query.apply(store.list())
}
