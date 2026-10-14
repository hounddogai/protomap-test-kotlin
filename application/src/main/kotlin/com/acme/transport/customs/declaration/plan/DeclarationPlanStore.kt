package com.acme.transport.customs.declaration.plan

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration plans by code. */
interface DeclarationPlanStore {
    fun find(code: String): DeclarationPlan?
    fun save(item: DeclarationPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationPlanStore(
    var items: MutableMap<String, DeclarationPlan>,
    var order: MutableList<String>,
) : DeclarationPlanStore {
    override fun find(code: String): DeclarationPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationPlan) {
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

    override fun list(): MutableList<DeclarationPlan> {
        val out = mutableListOf<DeclarationPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationPlanStore(): DeclarationPlanStore {
    return MemoryDeclarationPlanStore(mutableMapOf<String, DeclarationPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationPlanStore(
    var inner: DeclarationPlanStore,
    var cache: TtlCache<DeclarationPlan>,
    var hits: Int,
    var misses: Int,
) : DeclarationPlanStore {
    override fun find(code: String): DeclarationPlan? {
        val hit = (cache.get(code) as DeclarationPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationPlan> = inner.list()
}

fun newCachedDeclarationPlanStore(inner: DeclarationPlanStore, ttl: Int, clock: Clock): DeclarationPlanStore {
    return CachedDeclarationPlanStore(
        inner = inner,
        cache = newTtlCache<DeclarationPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration plans. Empty fields match all. */
data class DeclarationPlanQuery(
    var source: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationPlan): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationPlan>): MutableList<DeclarationPlan> {
        val matched = filter(items) { candidate: DeclarationPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationPlans(store: DeclarationPlanStore, query: DeclarationPlanQuery): MutableList<DeclarationPlan> {
    return query.apply(store.list())
}
