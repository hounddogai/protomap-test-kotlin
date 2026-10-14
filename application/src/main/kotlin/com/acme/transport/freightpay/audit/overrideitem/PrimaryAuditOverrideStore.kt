package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary audit overrides by code. */
interface PrimaryAuditOverrideStore {
    fun find(code: String): PrimaryAuditOverride?
    fun save(item: PrimaryAuditOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAuditOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAuditOverrideStore(
    var items: MutableMap<String, PrimaryAuditOverride>,
    var order: MutableList<String>,
) : PrimaryAuditOverrideStore {
    override fun find(code: String): PrimaryAuditOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAuditOverride) {
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

    override fun list(): MutableList<PrimaryAuditOverride> {
        val out = mutableListOf<PrimaryAuditOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAuditOverrideStore(): PrimaryAuditOverrideStore {
    return MemoryPrimaryAuditOverrideStore(
        mutableMapOf<String, PrimaryAuditOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAuditOverrideStore(
    var inner: PrimaryAuditOverrideStore,
    var cache: TtlCache<PrimaryAuditOverride>,
    var hits: Int,
    var misses: Int,
) : PrimaryAuditOverrideStore {
    override fun find(code: String): PrimaryAuditOverride? {
        val hit = (cache.get(code) as PrimaryAuditOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAuditOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAuditOverride> = inner.list()
}

fun newCachedPrimaryAuditOverrideStore(inner: PrimaryAuditOverrideStore, ttl: Int, clock: Clock): PrimaryAuditOverrideStore {
    return CachedPrimaryAuditOverrideStore(
        inner = inner,
        cache = newTtlCache<PrimaryAuditOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary audit overrides. Empty fields match all. */
data class PrimaryAuditOverrideQuery(
    var category: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAuditOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryAuditOverride>): MutableList<PrimaryAuditOverride> {
        val matched = filter(items) { candidate: PrimaryAuditOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAuditOverrides(store: PrimaryAuditOverrideStore, query: PrimaryAuditOverrideQuery): MutableList<PrimaryAuditOverride> {
    return query.apply(store.list())
}
