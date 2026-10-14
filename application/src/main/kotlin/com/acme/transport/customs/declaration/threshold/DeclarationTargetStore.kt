package com.acme.transport.customs.declaration.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration targets by code. */
interface DeclarationTargetStore {
    fun find(code: String): DeclarationTarget?
    fun save(item: DeclarationTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationTargetStore(
    var items: MutableMap<String, DeclarationTarget>,
    var order: MutableList<String>,
) : DeclarationTargetStore {
    override fun find(code: String): DeclarationTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationTarget) {
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

    override fun list(): MutableList<DeclarationTarget> {
        val out = mutableListOf<DeclarationTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationTargetStore(): DeclarationTargetStore {
    return MemoryDeclarationTargetStore(mutableMapOf<String, DeclarationTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationTargetStore(
    var inner: DeclarationTargetStore,
    var cache: TtlCache<DeclarationTarget>,
    var hits: Int,
    var misses: Int,
) : DeclarationTargetStore {
    override fun find(code: String): DeclarationTarget? {
        val hit = (cache.get(code) as DeclarationTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationTarget> = inner.list()
}

fun newCachedDeclarationTargetStore(inner: DeclarationTargetStore, ttl: Int, clock: Clock): DeclarationTargetStore {
    return CachedDeclarationTargetStore(
        inner = inner,
        cache = newTtlCache<DeclarationTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration targets. Empty fields match all. */
data class DeclarationTargetQuery(
    var country: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationTarget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationTarget>): MutableList<DeclarationTarget> {
        val matched = filter(items) { candidate: DeclarationTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationTargets(store: DeclarationTargetStore, query: DeclarationTargetQuery): MutableList<DeclarationTarget> {
    return query.apply(store.list())
}
