package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped zone baselines by code. */
interface CappedZoneBaselineStore {
    fun find(code: String): CappedZoneBaseline?
    fun save(item: CappedZoneBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedZoneBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedZoneBaselineStore(
    var items: MutableMap<String, CappedZoneBaseline>,
    var order: MutableList<String>,
) : CappedZoneBaselineStore {
    override fun find(code: String): CappedZoneBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedZoneBaseline) {
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

    override fun list(): MutableList<CappedZoneBaseline> {
        val out = mutableListOf<CappedZoneBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedZoneBaselineStore(): CappedZoneBaselineStore {
    return MemoryCappedZoneBaselineStore(mutableMapOf<String, CappedZoneBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedZoneBaselineStore(
    var inner: CappedZoneBaselineStore,
    var cache: TtlCache<CappedZoneBaseline>,
    var hits: Int,
    var misses: Int,
) : CappedZoneBaselineStore {
    override fun find(code: String): CappedZoneBaseline? {
        val hit = (cache.get(code) as CappedZoneBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedZoneBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedZoneBaseline> = inner.list()
}

fun newCachedCappedZoneBaselineStore(inner: CappedZoneBaselineStore, ttl: Int, clock: Clock): CappedZoneBaselineStore {
    return CachedCappedZoneBaselineStore(
        inner = inner,
        cache = newTtlCache<CappedZoneBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped zone baselines. Empty fields match all. */
data class CappedZoneBaselineQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedZoneBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedZoneBaseline>): MutableList<CappedZoneBaseline> {
        val matched = filter(items) { candidate: CappedZoneBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedZoneBaselines(store: CappedZoneBaselineStore, query: CappedZoneBaselineQuery): MutableList<CappedZoneBaseline> {
    return query.apply(store.list())
}
