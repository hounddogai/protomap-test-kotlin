package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge baselines by code. */
interface EvChargeBaselineStore {
    fun find(code: String): EvChargeBaseline?
    fun save(item: EvChargeBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeBaselineStore(
    var items: MutableMap<String, EvChargeBaseline>,
    var order: MutableList<String>,
) : EvChargeBaselineStore {
    override fun find(code: String): EvChargeBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeBaseline) {
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

    override fun list(): MutableList<EvChargeBaseline> {
        val out = mutableListOf<EvChargeBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeBaselineStore(): EvChargeBaselineStore {
    return MemoryEvChargeBaselineStore(mutableMapOf<String, EvChargeBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeBaselineStore(
    var inner: EvChargeBaselineStore,
    var cache: TtlCache<EvChargeBaseline>,
    var hits: Int,
    var misses: Int,
) : EvChargeBaselineStore {
    override fun find(code: String): EvChargeBaseline? {
        val hit = (cache.get(code) as EvChargeBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeBaseline> = inner.list()
}

fun newCachedEvChargeBaselineStore(inner: EvChargeBaselineStore, ttl: Int, clock: Clock): EvChargeBaselineStore {
    return CachedEvChargeBaselineStore(
        inner = inner,
        cache = newTtlCache<EvChargeBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge baselines. Empty fields match all. */
data class EvChargeBaselineQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeBaseline>): MutableList<EvChargeBaseline> {
        val matched = filter(items) { candidate: EvChargeBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeBaselines(store: EvChargeBaselineStore, query: EvChargeBaselineQuery): MutableList<EvChargeBaseline> {
    return query.apply(store.list())
}
