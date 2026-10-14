package com.acme.transport.emissions.idling.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling baselines by code. */
interface IdlingBaselineStore {
    fun find(code: String): IdlingBaseline?
    fun save(item: IdlingBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingBaselineStore(
    var items: MutableMap<String, IdlingBaseline>,
    var order: MutableList<String>,
) : IdlingBaselineStore {
    override fun find(code: String): IdlingBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingBaseline) {
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

    override fun list(): MutableList<IdlingBaseline> {
        val out = mutableListOf<IdlingBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingBaselineStore(): IdlingBaselineStore {
    return MemoryIdlingBaselineStore(mutableMapOf<String, IdlingBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingBaselineStore(
    var inner: IdlingBaselineStore,
    var cache: TtlCache<IdlingBaseline>,
    var hits: Int,
    var misses: Int,
) : IdlingBaselineStore {
    override fun find(code: String): IdlingBaseline? {
        val hit = (cache.get(code) as IdlingBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingBaseline> = inner.list()
}

fun newCachedIdlingBaselineStore(inner: IdlingBaselineStore, ttl: Int, clock: Clock): IdlingBaselineStore {
    return CachedIdlingBaselineStore(
        inner = inner,
        cache = newTtlCache<IdlingBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling baselines. Empty fields match all. */
data class IdlingBaselineQuery(
    var channel: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingBaseline): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingBaseline>): MutableList<IdlingBaseline> {
        val matched = filter(items) { candidate: IdlingBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingBaselines(store: IdlingBaselineStore, query: IdlingBaselineQuery): MutableList<IdlingBaseline> {
    return query.apply(store.list())
}
