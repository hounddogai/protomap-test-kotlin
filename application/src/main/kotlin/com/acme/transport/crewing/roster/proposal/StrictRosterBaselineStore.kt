package com.acme.transport.crewing.roster.proposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict roster baselines by code. */
interface StrictRosterBaselineStore {
    fun find(code: String): StrictRosterBaseline?
    fun save(item: StrictRosterBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRosterBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRosterBaselineStore(
    var items: MutableMap<String, StrictRosterBaseline>,
    var order: MutableList<String>,
) : StrictRosterBaselineStore {
    override fun find(code: String): StrictRosterBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRosterBaseline) {
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

    override fun list(): MutableList<StrictRosterBaseline> {
        val out = mutableListOf<StrictRosterBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRosterBaselineStore(): StrictRosterBaselineStore {
    return MemoryStrictRosterBaselineStore(
        mutableMapOf<String, StrictRosterBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRosterBaselineStore(
    var inner: StrictRosterBaselineStore,
    var cache: TtlCache<StrictRosterBaseline>,
    var hits: Int,
    var misses: Int,
) : StrictRosterBaselineStore {
    override fun find(code: String): StrictRosterBaseline? {
        val hit = (cache.get(code) as StrictRosterBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRosterBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRosterBaseline> = inner.list()
}

fun newCachedStrictRosterBaselineStore(inner: StrictRosterBaselineStore, ttl: Int, clock: Clock): StrictRosterBaselineStore {
    return CachedStrictRosterBaselineStore(
        inner = inner,
        cache = newTtlCache<StrictRosterBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict roster baselines. Empty fields match all. */
data class StrictRosterBaselineQuery(
    var locale: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRosterBaseline): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRosterBaseline>): MutableList<StrictRosterBaseline> {
        val matched = filter(items) { candidate: StrictRosterBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRosterBaselines(store: StrictRosterBaselineStore, query: StrictRosterBaselineQuery): MutableList<StrictRosterBaseline> {
    return query.apply(store.list())
}
