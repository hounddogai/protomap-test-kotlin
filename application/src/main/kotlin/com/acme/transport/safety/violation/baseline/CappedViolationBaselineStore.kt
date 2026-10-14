package com.acme.transport.safety.violation.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped violation baselines by code. */
interface CappedViolationBaselineStore {
    fun find(code: String): CappedViolationBaseline?
    fun save(item: CappedViolationBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedViolationBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedViolationBaselineStore(
    var items: MutableMap<String, CappedViolationBaseline>,
    var order: MutableList<String>,
) : CappedViolationBaselineStore {
    override fun find(code: String): CappedViolationBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedViolationBaseline) {
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

    override fun list(): MutableList<CappedViolationBaseline> {
        val out = mutableListOf<CappedViolationBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedViolationBaselineStore(): CappedViolationBaselineStore {
    return MemoryCappedViolationBaselineStore(
        mutableMapOf<String, CappedViolationBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedViolationBaselineStore(
    var inner: CappedViolationBaselineStore,
    var cache: TtlCache<CappedViolationBaseline>,
    var hits: Int,
    var misses: Int,
) : CappedViolationBaselineStore {
    override fun find(code: String): CappedViolationBaseline? {
        val hit = (cache.get(code) as CappedViolationBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedViolationBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedViolationBaseline> = inner.list()
}

fun newCachedCappedViolationBaselineStore(inner: CappedViolationBaselineStore, ttl: Int, clock: Clock): CappedViolationBaselineStore {
    return CachedCappedViolationBaselineStore(
        inner = inner,
        cache = newTtlCache<CappedViolationBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped violation baselines. Empty fields match all. */
data class CappedViolationBaselineQuery(
    var country: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedViolationBaseline): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedViolationBaseline>): MutableList<CappedViolationBaseline> {
        val matched = filter(items) { candidate: CappedViolationBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedViolationBaselines(store: CappedViolationBaselineStore, query: CappedViolationBaselineQuery): MutableList<CappedViolationBaseline> {
    return query.apply(store.list())
}
