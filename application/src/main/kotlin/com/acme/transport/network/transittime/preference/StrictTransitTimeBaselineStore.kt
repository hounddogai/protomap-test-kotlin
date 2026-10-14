package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict transit time baselines by code. */
interface StrictTransitTimeBaselineStore {
    fun find(code: String): StrictTransitTimeBaseline?
    fun save(item: StrictTransitTimeBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTransitTimeBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTransitTimeBaselineStore(
    var items: MutableMap<String, StrictTransitTimeBaseline>,
    var order: MutableList<String>,
) : StrictTransitTimeBaselineStore {
    override fun find(code: String): StrictTransitTimeBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTransitTimeBaseline) {
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

    override fun list(): MutableList<StrictTransitTimeBaseline> {
        val out = mutableListOf<StrictTransitTimeBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTransitTimeBaselineStore(): StrictTransitTimeBaselineStore {
    return MemoryStrictTransitTimeBaselineStore(
        mutableMapOf<String, StrictTransitTimeBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTransitTimeBaselineStore(
    var inner: StrictTransitTimeBaselineStore,
    var cache: TtlCache<StrictTransitTimeBaseline>,
    var hits: Int,
    var misses: Int,
) : StrictTransitTimeBaselineStore {
    override fun find(code: String): StrictTransitTimeBaseline? {
        val hit = (cache.get(code) as StrictTransitTimeBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTransitTimeBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTransitTimeBaseline> = inner.list()
}

fun newCachedStrictTransitTimeBaselineStore(inner: StrictTransitTimeBaselineStore, ttl: Int, clock: Clock): StrictTransitTimeBaselineStore {
    return CachedStrictTransitTimeBaselineStore(
        inner = inner,
        cache = newTtlCache<StrictTransitTimeBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict transit time baselines. Empty fields match all. */
data class StrictTransitTimeBaselineQuery(
    var channel: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTransitTimeBaseline): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTransitTimeBaseline>): MutableList<StrictTransitTimeBaseline> {
        val matched = filter(items) { candidate: StrictTransitTimeBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTransitTimeBaselines(store: StrictTransitTimeBaselineStore, query: StrictTransitTimeBaselineQuery): MutableList<StrictTransitTimeBaseline> {
    return query.apply(store.list())
}
