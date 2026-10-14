package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict ev charge baselines by code. */
interface StrictEvChargeBaselineStore {
    fun find(code: String): StrictEvChargeBaseline?
    fun save(item: StrictEvChargeBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictEvChargeBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictEvChargeBaselineStore(
    var items: MutableMap<String, StrictEvChargeBaseline>,
    var order: MutableList<String>,
) : StrictEvChargeBaselineStore {
    override fun find(code: String): StrictEvChargeBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictEvChargeBaseline) {
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

    override fun list(): MutableList<StrictEvChargeBaseline> {
        val out = mutableListOf<StrictEvChargeBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictEvChargeBaselineStore(): StrictEvChargeBaselineStore {
    return MemoryStrictEvChargeBaselineStore(
        mutableMapOf<String, StrictEvChargeBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictEvChargeBaselineStore(
    var inner: StrictEvChargeBaselineStore,
    var cache: TtlCache<StrictEvChargeBaseline>,
    var hits: Int,
    var misses: Int,
) : StrictEvChargeBaselineStore {
    override fun find(code: String): StrictEvChargeBaseline? {
        val hit = (cache.get(code) as StrictEvChargeBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictEvChargeBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictEvChargeBaseline> = inner.list()
}

fun newCachedStrictEvChargeBaselineStore(inner: StrictEvChargeBaselineStore, ttl: Int, clock: Clock): StrictEvChargeBaselineStore {
    return CachedStrictEvChargeBaselineStore(
        inner = inner,
        cache = newTtlCache<StrictEvChargeBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict ev charge baselines. Empty fields match all. */
data class StrictEvChargeBaselineQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictEvChargeBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictEvChargeBaseline>): MutableList<StrictEvChargeBaseline> {
        val matched = filter(items) { candidate: StrictEvChargeBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictEvChargeBaselines(store: StrictEvChargeBaselineStore, query: StrictEvChargeBaselineQuery): MutableList<StrictEvChargeBaseline> {
    return query.apply(store.list())
}
