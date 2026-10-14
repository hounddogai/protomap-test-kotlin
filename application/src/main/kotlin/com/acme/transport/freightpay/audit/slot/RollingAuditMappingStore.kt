package com.acme.transport.freightpay.audit.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling audit mappings by code. */
interface RollingAuditMappingStore {
    fun find(code: String): RollingAuditMapping?
    fun save(item: RollingAuditMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAuditMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAuditMappingStore(
    var items: MutableMap<String, RollingAuditMapping>,
    var order: MutableList<String>,
) : RollingAuditMappingStore {
    override fun find(code: String): RollingAuditMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAuditMapping) {
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

    override fun list(): MutableList<RollingAuditMapping> {
        val out = mutableListOf<RollingAuditMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAuditMappingStore(): RollingAuditMappingStore {
    return MemoryRollingAuditMappingStore(mutableMapOf<String, RollingAuditMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAuditMappingStore(
    var inner: RollingAuditMappingStore,
    var cache: TtlCache<RollingAuditMapping>,
    var hits: Int,
    var misses: Int,
) : RollingAuditMappingStore {
    override fun find(code: String): RollingAuditMapping? {
        val hit = (cache.get(code) as RollingAuditMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAuditMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAuditMapping> = inner.list()
}

fun newCachedRollingAuditMappingStore(inner: RollingAuditMappingStore, ttl: Int, clock: Clock): RollingAuditMappingStore {
    return CachedRollingAuditMappingStore(
        inner = inner,
        cache = newTtlCache<RollingAuditMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling audit mappings. Empty fields match all. */
data class RollingAuditMappingQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAuditMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RollingAuditMapping>): MutableList<RollingAuditMapping> {
        val matched = filter(items) { candidate: RollingAuditMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAuditMappings(store: RollingAuditMappingStore, query: RollingAuditMappingQuery): MutableList<RollingAuditMapping> {
    return query.apply(store.list())
}
