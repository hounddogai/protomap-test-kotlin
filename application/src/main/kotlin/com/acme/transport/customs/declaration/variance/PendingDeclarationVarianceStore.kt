package com.acme.transport.customs.declaration.variance

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending declaration variances by code. */
interface PendingDeclarationVarianceStore {
    fun find(code: String): PendingDeclarationVariance?
    fun save(item: PendingDeclarationVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDeclarationVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDeclarationVarianceStore(
    var items: MutableMap<String, PendingDeclarationVariance>,
    var order: MutableList<String>,
) : PendingDeclarationVarianceStore {
    override fun find(code: String): PendingDeclarationVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDeclarationVariance) {
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

    override fun list(): MutableList<PendingDeclarationVariance> {
        val out = mutableListOf<PendingDeclarationVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDeclarationVarianceStore(): PendingDeclarationVarianceStore {
    return MemoryPendingDeclarationVarianceStore(
        mutableMapOf<String, PendingDeclarationVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDeclarationVarianceStore(
    var inner: PendingDeclarationVarianceStore,
    var cache: TtlCache<PendingDeclarationVariance>,
    var hits: Int,
    var misses: Int,
) : PendingDeclarationVarianceStore {
    override fun find(code: String): PendingDeclarationVariance? {
        val hit = (cache.get(code) as PendingDeclarationVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDeclarationVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDeclarationVariance> = inner.list()
}

fun newCachedPendingDeclarationVarianceStore(inner: PendingDeclarationVarianceStore, ttl: Int, clock: Clock): PendingDeclarationVarianceStore {
    return CachedPendingDeclarationVarianceStore(
        inner = inner,
        cache = newTtlCache<PendingDeclarationVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending declaration variances. Empty fields match all. */
data class PendingDeclarationVarianceQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDeclarationVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDeclarationVariance>): MutableList<PendingDeclarationVariance> {
        val matched = filter(items) { candidate: PendingDeclarationVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDeclarationVariances(store: PendingDeclarationVarianceStore, query: PendingDeclarationVarianceQuery): MutableList<PendingDeclarationVariance> {
    return query.apply(store.list())
}
