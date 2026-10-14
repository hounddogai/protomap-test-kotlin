package com.acme.transport.crewing.qualification.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global qualification variances by code. */
interface GlobalQualificationVarianceStore {
    fun find(code: String): GlobalQualificationVariance?
    fun save(item: GlobalQualificationVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalQualificationVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalQualificationVarianceStore(
    var items: MutableMap<String, GlobalQualificationVariance>,
    var order: MutableList<String>,
) : GlobalQualificationVarianceStore {
    override fun find(code: String): GlobalQualificationVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalQualificationVariance) {
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

    override fun list(): MutableList<GlobalQualificationVariance> {
        val out = mutableListOf<GlobalQualificationVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalQualificationVarianceStore(): GlobalQualificationVarianceStore {
    return MemoryGlobalQualificationVarianceStore(
        mutableMapOf<String, GlobalQualificationVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalQualificationVarianceStore(
    var inner: GlobalQualificationVarianceStore,
    var cache: TtlCache<GlobalQualificationVariance>,
    var hits: Int,
    var misses: Int,
) : GlobalQualificationVarianceStore {
    override fun find(code: String): GlobalQualificationVariance? {
        val hit = (cache.get(code) as GlobalQualificationVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalQualificationVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalQualificationVariance> = inner.list()
}

fun newCachedGlobalQualificationVarianceStore(inner: GlobalQualificationVarianceStore, ttl: Int, clock: Clock): GlobalQualificationVarianceStore {
    return CachedGlobalQualificationVarianceStore(
        inner = inner,
        cache = newTtlCache<GlobalQualificationVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global qualification variances. Empty fields match all. */
data class GlobalQualificationVarianceQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalQualificationVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalQualificationVariance>): MutableList<GlobalQualificationVariance> {
        val matched = filter(items) { candidate: GlobalQualificationVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalQualificationVariances(store: GlobalQualificationVarianceStore, query: GlobalQualificationVarianceQuery): MutableList<GlobalQualificationVariance> {
    return query.apply(store.list())
}
