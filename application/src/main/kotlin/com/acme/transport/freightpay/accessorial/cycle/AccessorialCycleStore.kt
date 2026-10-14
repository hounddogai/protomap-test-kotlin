package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial cycles by code. */
interface AccessorialCycleStore {
    fun find(code: String): AccessorialCycle?
    fun save(item: AccessorialCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialCycleStore(
    var items: MutableMap<String, AccessorialCycle>,
    var order: MutableList<String>,
) : AccessorialCycleStore {
    override fun find(code: String): AccessorialCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialCycle) {
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

    override fun list(): MutableList<AccessorialCycle> {
        val out = mutableListOf<AccessorialCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialCycleStore(): AccessorialCycleStore {
    return MemoryAccessorialCycleStore(mutableMapOf<String, AccessorialCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialCycleStore(
    var inner: AccessorialCycleStore,
    var cache: TtlCache<AccessorialCycle>,
    var hits: Int,
    var misses: Int,
) : AccessorialCycleStore {
    override fun find(code: String): AccessorialCycle? {
        val hit = (cache.get(code) as AccessorialCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialCycle> = inner.list()
}

fun newCachedAccessorialCycleStore(inner: AccessorialCycleStore, ttl: Int, clock: Clock): AccessorialCycleStore {
    return CachedAccessorialCycleStore(
        inner = inner,
        cache = newTtlCache<AccessorialCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial cycles. Empty fields match all. */
data class AccessorialCycleQuery(
    var category: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialCycle): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<AccessorialCycle>): MutableList<AccessorialCycle> {
        val matched = filter(items) { candidate: AccessorialCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialCycles(store: AccessorialCycleStore, query: AccessorialCycleQuery): MutableList<AccessorialCycle> {
    return query.apply(store.list())
}
