package com.acme.transport.freightpay.audit.notice

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional audit slots by code. */
interface RegionalAuditSlotStore {
    fun find(code: String): RegionalAuditSlot?
    fun save(item: RegionalAuditSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalAuditSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalAuditSlotStore(
    var items: MutableMap<String, RegionalAuditSlot>,
    var order: MutableList<String>,
) : RegionalAuditSlotStore {
    override fun find(code: String): RegionalAuditSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalAuditSlot) {
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

    override fun list(): MutableList<RegionalAuditSlot> {
        val out = mutableListOf<RegionalAuditSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalAuditSlotStore(): RegionalAuditSlotStore {
    return MemoryRegionalAuditSlotStore(mutableMapOf<String, RegionalAuditSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalAuditSlotStore(
    var inner: RegionalAuditSlotStore,
    var cache: TtlCache<RegionalAuditSlot>,
    var hits: Int,
    var misses: Int,
) : RegionalAuditSlotStore {
    override fun find(code: String): RegionalAuditSlot? {
        val hit = (cache.get(code) as RegionalAuditSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalAuditSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalAuditSlot> = inner.list()
}

fun newCachedRegionalAuditSlotStore(inner: RegionalAuditSlotStore, ttl: Int, clock: Clock): RegionalAuditSlotStore {
    return CachedRegionalAuditSlotStore(
        inner = inner,
        cache = newTtlCache<RegionalAuditSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional audit slots. Empty fields match all. */
data class RegionalAuditSlotQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalAuditSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalAuditSlot>): MutableList<RegionalAuditSlot> {
        val matched = filter(items) { candidate: RegionalAuditSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalAuditSlots(store: RegionalAuditSlotStore, query: RegionalAuditSlotQuery): MutableList<RegionalAuditSlot> {
    return query.apply(store.list())
}
