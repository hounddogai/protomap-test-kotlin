package com.acme.transport.customs.clearance.reservation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default clearance baselines by code. */
interface DefaultClearanceBaselineStore {
    fun find(code: String): DefaultClearanceBaseline?
    fun save(item: DefaultClearanceBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultClearanceBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultClearanceBaselineStore(
    var items: MutableMap<String, DefaultClearanceBaseline>,
    var order: MutableList<String>,
) : DefaultClearanceBaselineStore {
    override fun find(code: String): DefaultClearanceBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultClearanceBaseline) {
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

    override fun list(): MutableList<DefaultClearanceBaseline> {
        val out = mutableListOf<DefaultClearanceBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultClearanceBaselineStore(): DefaultClearanceBaselineStore {
    return MemoryDefaultClearanceBaselineStore(
        mutableMapOf<String, DefaultClearanceBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultClearanceBaselineStore(
    var inner: DefaultClearanceBaselineStore,
    var cache: TtlCache<DefaultClearanceBaseline>,
    var hits: Int,
    var misses: Int,
) : DefaultClearanceBaselineStore {
    override fun find(code: String): DefaultClearanceBaseline? {
        val hit = (cache.get(code) as DefaultClearanceBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultClearanceBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultClearanceBaseline> = inner.list()
}

fun newCachedDefaultClearanceBaselineStore(inner: DefaultClearanceBaselineStore, ttl: Int, clock: Clock): DefaultClearanceBaselineStore {
    return CachedDefaultClearanceBaselineStore(
        inner = inner,
        cache = newTtlCache<DefaultClearanceBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default clearance baselines. Empty fields match all. */
data class DefaultClearanceBaselineQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultClearanceBaseline): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultClearanceBaseline>): MutableList<DefaultClearanceBaseline> {
        val matched = filter(items) { candidate: DefaultClearanceBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultClearanceBaselines(store: DefaultClearanceBaselineStore, query: DefaultClearanceBaselineQuery): MutableList<DefaultClearanceBaseline> {
    return query.apply(store.list())
}
