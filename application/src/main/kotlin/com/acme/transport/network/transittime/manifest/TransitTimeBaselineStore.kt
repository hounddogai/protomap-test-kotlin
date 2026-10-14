package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time baselines by code. */
interface TransitTimeBaselineStore {
    fun find(code: String): TransitTimeBaseline?
    fun save(item: TransitTimeBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeBaselineStore(
    var items: MutableMap<String, TransitTimeBaseline>,
    var order: MutableList<String>,
) : TransitTimeBaselineStore {
    override fun find(code: String): TransitTimeBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeBaseline) {
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

    override fun list(): MutableList<TransitTimeBaseline> {
        val out = mutableListOf<TransitTimeBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeBaselineStore(): TransitTimeBaselineStore {
    return MemoryTransitTimeBaselineStore(mutableMapOf<String, TransitTimeBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeBaselineStore(
    var inner: TransitTimeBaselineStore,
    var cache: TtlCache<TransitTimeBaseline>,
    var hits: Int,
    var misses: Int,
) : TransitTimeBaselineStore {
    override fun find(code: String): TransitTimeBaseline? {
        val hit = (cache.get(code) as TransitTimeBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeBaseline> = inner.list()
}

fun newCachedTransitTimeBaselineStore(inner: TransitTimeBaselineStore, ttl: Int, clock: Clock): TransitTimeBaselineStore {
    return CachedTransitTimeBaselineStore(
        inner = inner,
        cache = newTtlCache<TransitTimeBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time baselines. Empty fields match all. */
data class TransitTimeBaselineQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeBaseline>): MutableList<TransitTimeBaseline> {
        val matched = filter(items) { candidate: TransitTimeBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeBaselines(store: TransitTimeBaselineStore, query: TransitTimeBaselineQuery): MutableList<TransitTimeBaseline> {
    return query.apply(store.list())
}
