package com.acme.transport.crewing.roster.template

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster baselines by code. */
interface RosterBaselineStore {
    fun find(code: String): RosterBaseline?
    fun save(item: RosterBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterBaselineStore(
    var items: MutableMap<String, RosterBaseline>,
    var order: MutableList<String>,
) : RosterBaselineStore {
    override fun find(code: String): RosterBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterBaseline) {
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

    override fun list(): MutableList<RosterBaseline> {
        val out = mutableListOf<RosterBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterBaselineStore(): RosterBaselineStore {
    return MemoryRosterBaselineStore(mutableMapOf<String, RosterBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterBaselineStore(
    var inner: RosterBaselineStore,
    var cache: TtlCache<RosterBaseline>,
    var hits: Int,
    var misses: Int,
) : RosterBaselineStore {
    override fun find(code: String): RosterBaseline? {
        val hit = (cache.get(code) as RosterBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterBaseline> = inner.list()
}

fun newCachedRosterBaselineStore(inner: RosterBaselineStore, ttl: Int, clock: Clock): RosterBaselineStore {
    return CachedRosterBaselineStore(
        inner = inner,
        cache = newTtlCache<RosterBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster baselines. Empty fields match all. */
data class RosterBaselineQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterBaseline>): MutableList<RosterBaseline> {
        val matched = filter(items) { candidate: RosterBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterBaselines(store: RosterBaselineStore, query: RosterBaselineQuery): MutableList<RosterBaseline> {
    return query.apply(store.list())
}
