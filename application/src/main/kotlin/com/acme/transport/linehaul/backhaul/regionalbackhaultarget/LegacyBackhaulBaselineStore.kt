package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy backhaul baselines by code. */
interface LegacyBackhaulBaselineStore {
    fun find(code: String): LegacyBackhaulBaseline?
    fun save(item: LegacyBackhaulBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyBackhaulBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyBackhaulBaselineStore(
    var items: MutableMap<String, LegacyBackhaulBaseline>,
    var order: MutableList<String>,
) : LegacyBackhaulBaselineStore {
    override fun find(code: String): LegacyBackhaulBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyBackhaulBaseline) {
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

    override fun list(): MutableList<LegacyBackhaulBaseline> {
        val out = mutableListOf<LegacyBackhaulBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyBackhaulBaselineStore(): LegacyBackhaulBaselineStore {
    return MemoryLegacyBackhaulBaselineStore(
        mutableMapOf<String, LegacyBackhaulBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyBackhaulBaselineStore(
    var inner: LegacyBackhaulBaselineStore,
    var cache: TtlCache<LegacyBackhaulBaseline>,
    var hits: Int,
    var misses: Int,
) : LegacyBackhaulBaselineStore {
    override fun find(code: String): LegacyBackhaulBaseline? {
        val hit = (cache.get(code) as LegacyBackhaulBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyBackhaulBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyBackhaulBaseline> = inner.list()
}

fun newCachedLegacyBackhaulBaselineStore(inner: LegacyBackhaulBaselineStore, ttl: Int, clock: Clock): LegacyBackhaulBaselineStore {
    return CachedLegacyBackhaulBaselineStore(
        inner = inner,
        cache = newTtlCache<LegacyBackhaulBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy backhaul baselines. Empty fields match all. */
data class LegacyBackhaulBaselineQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyBackhaulBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyBackhaulBaseline>): MutableList<LegacyBackhaulBaseline> {
        val matched = filter(items) { candidate: LegacyBackhaulBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyBackhaulBaselines(store: LegacyBackhaulBaselineStore, query: LegacyBackhaulBaselineQuery): MutableList<LegacyBackhaulBaseline> {
    return query.apply(store.list())
}
