package com.acme.transport.customs.declaration.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration estimates by code. */
interface DeclarationEstimateStore {
    fun find(code: String): DeclarationEstimate?
    fun save(item: DeclarationEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationEstimateStore(
    var items: MutableMap<String, DeclarationEstimate>,
    var order: MutableList<String>,
) : DeclarationEstimateStore {
    override fun find(code: String): DeclarationEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationEstimate) {
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

    override fun list(): MutableList<DeclarationEstimate> {
        val out = mutableListOf<DeclarationEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationEstimateStore(): DeclarationEstimateStore {
    return MemoryDeclarationEstimateStore(mutableMapOf<String, DeclarationEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationEstimateStore(
    var inner: DeclarationEstimateStore,
    var cache: TtlCache<DeclarationEstimate>,
    var hits: Int,
    var misses: Int,
) : DeclarationEstimateStore {
    override fun find(code: String): DeclarationEstimate? {
        val hit = (cache.get(code) as DeclarationEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationEstimate> = inner.list()
}

fun newCachedDeclarationEstimateStore(inner: DeclarationEstimateStore, ttl: Int, clock: Clock): DeclarationEstimateStore {
    return CachedDeclarationEstimateStore(
        inner = inner,
        cache = newTtlCache<DeclarationEstimate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration estimates. Empty fields match all. */
data class DeclarationEstimateQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationEstimate): Boolean {
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

    fun apply(items: MutableList<DeclarationEstimate>): MutableList<DeclarationEstimate> {
        val matched = filter(items) { candidate: DeclarationEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationEstimates(store: DeclarationEstimateStore, query: DeclarationEstimateQuery): MutableList<DeclarationEstimate> {
    return query.apply(store.list())
}
