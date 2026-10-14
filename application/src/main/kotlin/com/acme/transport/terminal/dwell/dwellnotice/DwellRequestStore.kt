package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell requests by code. */
interface DwellRequestStore {
    fun find(code: String): DwellRequest?
    fun save(item: DwellRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellRequestStore(
    var items: MutableMap<String, DwellRequest>,
    var order: MutableList<String>,
) : DwellRequestStore {
    override fun find(code: String): DwellRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellRequest) {
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

    override fun list(): MutableList<DwellRequest> {
        val out = mutableListOf<DwellRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellRequestStore(): DwellRequestStore {
    return MemoryDwellRequestStore(mutableMapOf<String, DwellRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellRequestStore(
    var inner: DwellRequestStore,
    var cache: TtlCache<DwellRequest>,
    var hits: Int,
    var misses: Int,
) : DwellRequestStore {
    override fun find(code: String): DwellRequest? {
        val hit = (cache.get(code) as DwellRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellRequest> = inner.list()
}

fun newCachedDwellRequestStore(inner: DwellRequestStore, ttl: Int, clock: Clock): DwellRequestStore {
    return CachedDwellRequestStore(
        inner = inner,
        cache = newTtlCache<DwellRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell requests. Empty fields match all. */
data class DwellRequestQuery(
    var unit: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellRequest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<DwellRequest>): MutableList<DwellRequest> {
        val matched = filter(items) { candidate: DwellRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellRequests(store: DwellRequestStore, query: DwellRequestQuery): MutableList<DwellRequest> {
    return query.apply(store.list())
}
