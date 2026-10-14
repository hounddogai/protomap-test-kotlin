package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional backhaul baselines by code. */
interface RegionalBackhaulBaselineStore {
    fun find(code: String): RegionalBackhaulBaseline?
    fun save(item: RegionalBackhaulBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalBackhaulBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalBackhaulBaselineStore(
    var items: MutableMap<String, RegionalBackhaulBaseline>,
    var order: MutableList<String>,
) : RegionalBackhaulBaselineStore {
    override fun find(code: String): RegionalBackhaulBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalBackhaulBaseline) {
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

    override fun list(): MutableList<RegionalBackhaulBaseline> {
        val out = mutableListOf<RegionalBackhaulBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalBackhaulBaselineStore(): RegionalBackhaulBaselineStore {
    return MemoryRegionalBackhaulBaselineStore(
        mutableMapOf<String, RegionalBackhaulBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalBackhaulBaselineStore(
    var inner: RegionalBackhaulBaselineStore,
    var cache: TtlCache<RegionalBackhaulBaseline>,
    var hits: Int,
    var misses: Int,
) : RegionalBackhaulBaselineStore {
    override fun find(code: String): RegionalBackhaulBaseline? {
        val hit = (cache.get(code) as RegionalBackhaulBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalBackhaulBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalBackhaulBaseline> = inner.list()
}

fun newCachedRegionalBackhaulBaselineStore(inner: RegionalBackhaulBaselineStore, ttl: Int, clock: Clock): RegionalBackhaulBaselineStore {
    return CachedRegionalBackhaulBaselineStore(
        inner = inner,
        cache = newTtlCache<RegionalBackhaulBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional backhaul baselines. Empty fields match all. */
data class RegionalBackhaulBaselineQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalBackhaulBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RegionalBackhaulBaseline>): MutableList<RegionalBackhaulBaseline> {
        val matched = filter(items) { candidate: RegionalBackhaulBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalBackhaulBaselines(store: RegionalBackhaulBaselineStore, query: RegionalBackhaulBaselineQuery): MutableList<RegionalBackhaulBaseline> {
    return query.apply(store.list())
}
