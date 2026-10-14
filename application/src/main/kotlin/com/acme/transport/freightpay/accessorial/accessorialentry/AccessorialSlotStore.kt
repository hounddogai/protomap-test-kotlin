package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial slots by code. */
interface AccessorialSlotStore {
    fun find(code: String): AccessorialSlot?
    fun save(item: AccessorialSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialSlotStore(
    var items: MutableMap<String, AccessorialSlot>,
    var order: MutableList<String>,
) : AccessorialSlotStore {
    override fun find(code: String): AccessorialSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialSlot) {
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

    override fun list(): MutableList<AccessorialSlot> {
        val out = mutableListOf<AccessorialSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialSlotStore(): AccessorialSlotStore {
    return MemoryAccessorialSlotStore(mutableMapOf<String, AccessorialSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialSlotStore(
    var inner: AccessorialSlotStore,
    var cache: TtlCache<AccessorialSlot>,
    var hits: Int,
    var misses: Int,
) : AccessorialSlotStore {
    override fun find(code: String): AccessorialSlot? {
        val hit = (cache.get(code) as AccessorialSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialSlot> = inner.list()
}

fun newCachedAccessorialSlotStore(inner: AccessorialSlotStore, ttl: Int, clock: Clock): AccessorialSlotStore {
    return CachedAccessorialSlotStore(
        inner = inner,
        cache = newTtlCache<AccessorialSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial slots. Empty fields match all. */
data class AccessorialSlotQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialSlot>): MutableList<AccessorialSlot> {
        val matched = filter(items) { candidate: AccessorialSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialSlots(store: AccessorialSlotStore, query: AccessorialSlotQuery): MutableList<AccessorialSlot> {
    return query.apply(store.list())
}
