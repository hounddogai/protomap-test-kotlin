package com.acme.transport.freightpay.audit.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual audit slots by code. */
interface ManualAuditSlotStore {
    fun find(code: String): ManualAuditSlot?
    fun save(item: ManualAuditSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAuditSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAuditSlotStore(
    var items: MutableMap<String, ManualAuditSlot>,
    var order: MutableList<String>,
) : ManualAuditSlotStore {
    override fun find(code: String): ManualAuditSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAuditSlot) {
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

    override fun list(): MutableList<ManualAuditSlot> {
        val out = mutableListOf<ManualAuditSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAuditSlotStore(): ManualAuditSlotStore {
    return MemoryManualAuditSlotStore(mutableMapOf<String, ManualAuditSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAuditSlotStore(
    var inner: ManualAuditSlotStore,
    var cache: TtlCache<ManualAuditSlot>,
    var hits: Int,
    var misses: Int,
) : ManualAuditSlotStore {
    override fun find(code: String): ManualAuditSlot? {
        val hit = (cache.get(code) as ManualAuditSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAuditSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAuditSlot> = inner.list()
}

fun newCachedManualAuditSlotStore(inner: ManualAuditSlotStore, ttl: Int, clock: Clock): ManualAuditSlotStore {
    return CachedManualAuditSlotStore(
        inner = inner,
        cache = newTtlCache<ManualAuditSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual audit slots. Empty fields match all. */
data class ManualAuditSlotQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAuditSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAuditSlot>): MutableList<ManualAuditSlot> {
        val matched = filter(items) { candidate: ManualAuditSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAuditSlots(store: ManualAuditSlotStore, query: ManualAuditSlotQuery): MutableList<ManualAuditSlot> {
    return query.apply(store.list())
}
