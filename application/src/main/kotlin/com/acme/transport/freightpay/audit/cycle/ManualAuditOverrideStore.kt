package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual audit overrides by code. */
interface ManualAuditOverrideStore {
    fun find(code: String): ManualAuditOverride?
    fun save(item: ManualAuditOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAuditOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAuditOverrideStore(
    var items: MutableMap<String, ManualAuditOverride>,
    var order: MutableList<String>,
) : ManualAuditOverrideStore {
    override fun find(code: String): ManualAuditOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAuditOverride) {
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

    override fun list(): MutableList<ManualAuditOverride> {
        val out = mutableListOf<ManualAuditOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAuditOverrideStore(): ManualAuditOverrideStore {
    return MemoryManualAuditOverrideStore(mutableMapOf<String, ManualAuditOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAuditOverrideStore(
    var inner: ManualAuditOverrideStore,
    var cache: TtlCache<ManualAuditOverride>,
    var hits: Int,
    var misses: Int,
) : ManualAuditOverrideStore {
    override fun find(code: String): ManualAuditOverride? {
        val hit = (cache.get(code) as ManualAuditOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAuditOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAuditOverride> = inner.list()
}

fun newCachedManualAuditOverrideStore(inner: ManualAuditOverrideStore, ttl: Int, clock: Clock): ManualAuditOverrideStore {
    return CachedManualAuditOverrideStore(
        inner = inner,
        cache = newTtlCache<ManualAuditOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual audit overrides. Empty fields match all. */
data class ManualAuditOverrideQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAuditOverride): Boolean {
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

    fun apply(items: MutableList<ManualAuditOverride>): MutableList<ManualAuditOverride> {
        val matched = filter(items) { candidate: ManualAuditOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAuditOverrides(store: ManualAuditOverrideStore, query: ManualAuditOverrideQuery): MutableList<ManualAuditOverride> {
    return query.apply(store.list())
}
