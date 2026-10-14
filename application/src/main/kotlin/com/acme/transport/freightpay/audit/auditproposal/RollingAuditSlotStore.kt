package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling audit slots by code. */
interface RollingAuditSlotStore {
    fun find(code: String): RollingAuditSlot?
    fun save(item: RollingAuditSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAuditSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAuditSlotStore(
    var items: MutableMap<String, RollingAuditSlot>,
    var order: MutableList<String>,
) : RollingAuditSlotStore {
    override fun find(code: String): RollingAuditSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAuditSlot) {
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

    override fun list(): MutableList<RollingAuditSlot> {
        val out = mutableListOf<RollingAuditSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAuditSlotStore(): RollingAuditSlotStore {
    return MemoryRollingAuditSlotStore(mutableMapOf<String, RollingAuditSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAuditSlotStore(
    var inner: RollingAuditSlotStore,
    var cache: TtlCache<RollingAuditSlot>,
    var hits: Int,
    var misses: Int,
) : RollingAuditSlotStore {
    override fun find(code: String): RollingAuditSlot? {
        val hit = (cache.get(code) as RollingAuditSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAuditSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAuditSlot> = inner.list()
}

fun newCachedRollingAuditSlotStore(inner: RollingAuditSlotStore, ttl: Int, clock: Clock): RollingAuditSlotStore {
    return CachedRollingAuditSlotStore(
        inner = inner,
        cache = newTtlCache<RollingAuditSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling audit slots. Empty fields match all. */
data class RollingAuditSlotQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAuditSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAuditSlot>): MutableList<RollingAuditSlot> {
        val matched = filter(items) { candidate: RollingAuditSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAuditSlots(store: RollingAuditSlotStore, query: RollingAuditSlotQuery): MutableList<RollingAuditSlot> {
    return query.apply(store.list())
}
