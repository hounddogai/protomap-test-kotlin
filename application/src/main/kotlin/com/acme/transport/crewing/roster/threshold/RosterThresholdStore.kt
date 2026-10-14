package com.acme.transport.crewing.roster.threshold

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster thresholds by code. */
interface RosterThresholdStore {
    fun find(code: String): RosterThreshold?
    fun save(item: RosterThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterThresholdStore(
    var items: MutableMap<String, RosterThreshold>,
    var order: MutableList<String>,
) : RosterThresholdStore {
    override fun find(code: String): RosterThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterThreshold) {
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

    override fun list(): MutableList<RosterThreshold> {
        val out = mutableListOf<RosterThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterThresholdStore(): RosterThresholdStore {
    return MemoryRosterThresholdStore(mutableMapOf<String, RosterThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterThresholdStore(
    var inner: RosterThresholdStore,
    var cache: TtlCache<RosterThreshold>,
    var hits: Int,
    var misses: Int,
) : RosterThresholdStore {
    override fun find(code: String): RosterThreshold? {
        val hit = (cache.get(code) as RosterThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterThreshold> = inner.list()
}

fun newCachedRosterThresholdStore(inner: RosterThresholdStore, ttl: Int, clock: Clock): RosterThresholdStore {
    return CachedRosterThresholdStore(
        inner = inner,
        cache = newTtlCache<RosterThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster thresholds. Empty fields match all. */
data class RosterThresholdQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterThreshold>): MutableList<RosterThreshold> {
        val matched = filter(items) { candidate: RosterThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterThresholds(store: RosterThresholdStore, query: RosterThresholdQuery): MutableList<RosterThreshold> {
    return query.apply(store.list())
}
