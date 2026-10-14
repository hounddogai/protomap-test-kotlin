package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event budgets by code. */
interface CameraEventBudgetStore {
    fun find(code: String): CameraEventBudget?
    fun save(item: CameraEventBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventBudgetStore(
    var items: MutableMap<String, CameraEventBudget>,
    var order: MutableList<String>,
) : CameraEventBudgetStore {
    override fun find(code: String): CameraEventBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventBudget) {
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

    override fun list(): MutableList<CameraEventBudget> {
        val out = mutableListOf<CameraEventBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventBudgetStore(): CameraEventBudgetStore {
    return MemoryCameraEventBudgetStore(mutableMapOf<String, CameraEventBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventBudgetStore(
    var inner: CameraEventBudgetStore,
    var cache: TtlCache<CameraEventBudget>,
    var hits: Int,
    var misses: Int,
) : CameraEventBudgetStore {
    override fun find(code: String): CameraEventBudget? {
        val hit = (cache.get(code) as CameraEventBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventBudget> = inner.list()
}

fun newCachedCameraEventBudgetStore(inner: CameraEventBudgetStore, ttl: Int, clock: Clock): CameraEventBudgetStore {
    return CachedCameraEventBudgetStore(
        inner = inner,
        cache = newTtlCache<CameraEventBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event budgets. Empty fields match all. */
data class CameraEventBudgetQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<CameraEventBudget>): MutableList<CameraEventBudget> {
        val matched = filter(items) { candidate: CameraEventBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventBudgets(store: CameraEventBudgetStore, query: CameraEventBudgetQuery): MutableList<CameraEventBudget> {
    return query.apply(store.list())
}
