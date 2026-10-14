package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling backhaul baselines by code. */
interface RollingBackhaulBaselineStore {
    fun find(code: String): RollingBackhaulBaseline?
    fun save(item: RollingBackhaulBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingBackhaulBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingBackhaulBaselineStore(
    var items: MutableMap<String, RollingBackhaulBaseline>,
    var order: MutableList<String>,
) : RollingBackhaulBaselineStore {
    override fun find(code: String): RollingBackhaulBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingBackhaulBaseline) {
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

    override fun list(): MutableList<RollingBackhaulBaseline> {
        val out = mutableListOf<RollingBackhaulBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingBackhaulBaselineStore(): RollingBackhaulBaselineStore {
    return MemoryRollingBackhaulBaselineStore(
        mutableMapOf<String, RollingBackhaulBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingBackhaulBaselineStore(
    var inner: RollingBackhaulBaselineStore,
    var cache: TtlCache<RollingBackhaulBaseline>,
    var hits: Int,
    var misses: Int,
) : RollingBackhaulBaselineStore {
    override fun find(code: String): RollingBackhaulBaseline? {
        val hit = (cache.get(code) as RollingBackhaulBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingBackhaulBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingBackhaulBaseline> = inner.list()
}

fun newCachedRollingBackhaulBaselineStore(inner: RollingBackhaulBaselineStore, ttl: Int, clock: Clock): RollingBackhaulBaselineStore {
    return CachedRollingBackhaulBaselineStore(
        inner = inner,
        cache = newTtlCache<RollingBackhaulBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling backhaul baselines. Empty fields match all. */
data class RollingBackhaulBaselineQuery(
    var locale: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingBackhaulBaseline): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingBackhaulBaseline>): MutableList<RollingBackhaulBaseline> {
        val matched = filter(items) { candidate: RollingBackhaulBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingBackhaulBaselines(store: RollingBackhaulBaselineStore, query: RollingBackhaulBaselineQuery): MutableList<RollingBackhaulBaseline> {
    return query.apply(store.list())
}
