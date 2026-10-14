package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default accessorial cycles by code. */
interface DefaultAccessorialCycleStore {
    fun find(code: String): DefaultAccessorialCycle?
    fun save(item: DefaultAccessorialCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAccessorialCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAccessorialCycleStore(
    var items: MutableMap<String, DefaultAccessorialCycle>,
    var order: MutableList<String>,
) : DefaultAccessorialCycleStore {
    override fun find(code: String): DefaultAccessorialCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAccessorialCycle) {
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

    override fun list(): MutableList<DefaultAccessorialCycle> {
        val out = mutableListOf<DefaultAccessorialCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAccessorialCycleStore(): DefaultAccessorialCycleStore {
    return MemoryDefaultAccessorialCycleStore(
        mutableMapOf<String, DefaultAccessorialCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAccessorialCycleStore(
    var inner: DefaultAccessorialCycleStore,
    var cache: TtlCache<DefaultAccessorialCycle>,
    var hits: Int,
    var misses: Int,
) : DefaultAccessorialCycleStore {
    override fun find(code: String): DefaultAccessorialCycle? {
        val hit = (cache.get(code) as DefaultAccessorialCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAccessorialCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAccessorialCycle> = inner.list()
}

fun newCachedDefaultAccessorialCycleStore(inner: DefaultAccessorialCycleStore, ttl: Int, clock: Clock): DefaultAccessorialCycleStore {
    return CachedDefaultAccessorialCycleStore(
        inner = inner,
        cache = newTtlCache<DefaultAccessorialCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default accessorial cycles. Empty fields match all. */
data class DefaultAccessorialCycleQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAccessorialCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultAccessorialCycle>): MutableList<DefaultAccessorialCycle> {
        val matched = filter(items) { candidate: DefaultAccessorialCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAccessorialCycles(store: DefaultAccessorialCycleStore, query: DefaultAccessorialCycleQuery): MutableList<DefaultAccessorialCycle> {
    return query.apply(store.list())
}
