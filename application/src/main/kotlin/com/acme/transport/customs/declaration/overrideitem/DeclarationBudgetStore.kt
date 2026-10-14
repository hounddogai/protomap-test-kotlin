package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration budgets by code. */
interface DeclarationBudgetStore {
    fun find(code: String): DeclarationBudget?
    fun save(item: DeclarationBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationBudgetStore(
    var items: MutableMap<String, DeclarationBudget>,
    var order: MutableList<String>,
) : DeclarationBudgetStore {
    override fun find(code: String): DeclarationBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationBudget) {
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

    override fun list(): MutableList<DeclarationBudget> {
        val out = mutableListOf<DeclarationBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationBudgetStore(): DeclarationBudgetStore {
    return MemoryDeclarationBudgetStore(mutableMapOf<String, DeclarationBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationBudgetStore(
    var inner: DeclarationBudgetStore,
    var cache: TtlCache<DeclarationBudget>,
    var hits: Int,
    var misses: Int,
) : DeclarationBudgetStore {
    override fun find(code: String): DeclarationBudget? {
        val hit = (cache.get(code) as DeclarationBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationBudget> = inner.list()
}

fun newCachedDeclarationBudgetStore(inner: DeclarationBudgetStore, ttl: Int, clock: Clock): DeclarationBudgetStore {
    return CachedDeclarationBudgetStore(
        inner = inner,
        cache = newTtlCache<DeclarationBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration budgets. Empty fields match all. */
data class DeclarationBudgetQuery(
    var channel: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationBudget): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationBudget>): MutableList<DeclarationBudget> {
        val matched = filter(items) { candidate: DeclarationBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationBudgets(store: DeclarationBudgetStore, query: DeclarationBudgetQuery): MutableList<DeclarationBudget> {
    return query.apply(store.list())
}
