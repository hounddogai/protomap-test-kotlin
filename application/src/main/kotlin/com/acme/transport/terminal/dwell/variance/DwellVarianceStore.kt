package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell variances by code. */
interface DwellVarianceStore {
    fun find(code: String): DwellVariance?
    fun save(item: DwellVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellVarianceStore(
    var items: MutableMap<String, DwellVariance>,
    var order: MutableList<String>,
) : DwellVarianceStore {
    override fun find(code: String): DwellVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellVariance) {
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

    override fun list(): MutableList<DwellVariance> {
        val out = mutableListOf<DwellVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellVarianceStore(): DwellVarianceStore {
    return MemoryDwellVarianceStore(mutableMapOf<String, DwellVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellVarianceStore(
    var inner: DwellVarianceStore,
    var cache: TtlCache<DwellVariance>,
    var hits: Int,
    var misses: Int,
) : DwellVarianceStore {
    override fun find(code: String): DwellVariance? {
        val hit = (cache.get(code) as DwellVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellVariance> = inner.list()
}

fun newCachedDwellVarianceStore(inner: DwellVarianceStore, ttl: Int, clock: Clock): DwellVarianceStore {
    return CachedDwellVarianceStore(
        inner = inner,
        cache = newTtlCache<DwellVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell variances. Empty fields match all. */
data class DwellVarianceQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellVariance>): MutableList<DwellVariance> {
        val matched = filter(items) { candidate: DwellVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellVariances(store: DwellVarianceStore, query: DwellVarianceQuery): MutableList<DwellVariance> {
    return query.apply(store.list())
}
