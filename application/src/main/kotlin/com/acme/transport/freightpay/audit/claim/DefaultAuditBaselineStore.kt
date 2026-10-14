package com.acme.transport.freightpay.audit.claim

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default audit baselines by code. */
interface DefaultAuditBaselineStore {
    fun find(code: String): DefaultAuditBaseline?
    fun save(item: DefaultAuditBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAuditBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAuditBaselineStore(
    var items: MutableMap<String, DefaultAuditBaseline>,
    var order: MutableList<String>,
) : DefaultAuditBaselineStore {
    override fun find(code: String): DefaultAuditBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAuditBaseline) {
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

    override fun list(): MutableList<DefaultAuditBaseline> {
        val out = mutableListOf<DefaultAuditBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAuditBaselineStore(): DefaultAuditBaselineStore {
    return MemoryDefaultAuditBaselineStore(
        mutableMapOf<String, DefaultAuditBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAuditBaselineStore(
    var inner: DefaultAuditBaselineStore,
    var cache: TtlCache<DefaultAuditBaseline>,
    var hits: Int,
    var misses: Int,
) : DefaultAuditBaselineStore {
    override fun find(code: String): DefaultAuditBaseline? {
        val hit = (cache.get(code) as DefaultAuditBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAuditBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAuditBaseline> = inner.list()
}

fun newCachedDefaultAuditBaselineStore(inner: DefaultAuditBaselineStore, ttl: Int, clock: Clock): DefaultAuditBaselineStore {
    return CachedDefaultAuditBaselineStore(
        inner = inner,
        cache = newTtlCache<DefaultAuditBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default audit baselines. Empty fields match all. */
data class DefaultAuditBaselineQuery(
    var lane: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAuditBaseline): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultAuditBaseline>): MutableList<DefaultAuditBaseline> {
        val matched = filter(items) { candidate: DefaultAuditBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAuditBaselines(store: DefaultAuditBaselineStore, query: DefaultAuditBaselineQuery): MutableList<DefaultAuditBaseline> {
    return query.apply(store.list())
}
