package com.acme.transport.freightpay.accessorial.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial budgets by code. */
interface AccessorialBudgetStore {
    fun find(code: String): AccessorialBudget?
    fun save(item: AccessorialBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialBudgetStore(
    var items: MutableMap<String, AccessorialBudget>,
    var order: MutableList<String>,
) : AccessorialBudgetStore {
    override fun find(code: String): AccessorialBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialBudget) {
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

    override fun list(): MutableList<AccessorialBudget> {
        val out = mutableListOf<AccessorialBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialBudgetStore(): AccessorialBudgetStore {
    return MemoryAccessorialBudgetStore(mutableMapOf<String, AccessorialBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialBudgetStore(
    var inner: AccessorialBudgetStore,
    var cache: TtlCache<AccessorialBudget>,
    var hits: Int,
    var misses: Int,
) : AccessorialBudgetStore {
    override fun find(code: String): AccessorialBudget? {
        val hit = (cache.get(code) as AccessorialBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialBudget> = inner.list()
}

fun newCachedAccessorialBudgetStore(inner: AccessorialBudgetStore, ttl: Int, clock: Clock): AccessorialBudgetStore {
    return CachedAccessorialBudgetStore(
        inner = inner,
        cache = newTtlCache<AccessorialBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial budgets. Empty fields match all. */
data class AccessorialBudgetQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AccessorialBudget>): MutableList<AccessorialBudget> {
        val matched = filter(items) { candidate: AccessorialBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialBudgets(store: AccessorialBudgetStore, query: AccessorialBudgetQuery): MutableList<AccessorialBudget> {
    return query.apply(store.list())
}
