package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation baselines by code. */
interface ViolationBaselineStore {
    fun find(code: String): ViolationBaseline?
    fun save(item: ViolationBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationBaselineStore(
    var items: MutableMap<String, ViolationBaseline>,
    var order: MutableList<String>,
) : ViolationBaselineStore {
    override fun find(code: String): ViolationBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationBaseline) {
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

    override fun list(): MutableList<ViolationBaseline> {
        val out = mutableListOf<ViolationBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationBaselineStore(): ViolationBaselineStore {
    return MemoryViolationBaselineStore(mutableMapOf<String, ViolationBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationBaselineStore(
    var inner: ViolationBaselineStore,
    var cache: TtlCache<ViolationBaseline>,
    var hits: Int,
    var misses: Int,
) : ViolationBaselineStore {
    override fun find(code: String): ViolationBaseline? {
        val hit = (cache.get(code) as ViolationBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationBaseline> = inner.list()
}

fun newCachedViolationBaselineStore(inner: ViolationBaselineStore, ttl: Int, clock: Clock): ViolationBaselineStore {
    return CachedViolationBaselineStore(
        inner = inner,
        cache = newTtlCache<ViolationBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation baselines. Empty fields match all. */
data class ViolationBaselineQuery(
    var channel: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationBaseline): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<ViolationBaseline>): MutableList<ViolationBaseline> {
        val matched = filter(items) { candidate: ViolationBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationBaselines(store: ViolationBaselineStore, query: ViolationBaselineQuery): MutableList<ViolationBaseline> {
    return query.apply(store.list())
}
