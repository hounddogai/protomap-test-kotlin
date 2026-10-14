package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global coaching baselines by code. */
interface GlobalCoachingBaselineStore {
    fun find(code: String): GlobalCoachingBaseline?
    fun save(item: GlobalCoachingBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCoachingBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCoachingBaselineStore(
    var items: MutableMap<String, GlobalCoachingBaseline>,
    var order: MutableList<String>,
) : GlobalCoachingBaselineStore {
    override fun find(code: String): GlobalCoachingBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCoachingBaseline) {
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

    override fun list(): MutableList<GlobalCoachingBaseline> {
        val out = mutableListOf<GlobalCoachingBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCoachingBaselineStore(): GlobalCoachingBaselineStore {
    return MemoryGlobalCoachingBaselineStore(
        mutableMapOf<String, GlobalCoachingBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCoachingBaselineStore(
    var inner: GlobalCoachingBaselineStore,
    var cache: TtlCache<GlobalCoachingBaseline>,
    var hits: Int,
    var misses: Int,
) : GlobalCoachingBaselineStore {
    override fun find(code: String): GlobalCoachingBaseline? {
        val hit = (cache.get(code) as GlobalCoachingBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCoachingBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCoachingBaseline> = inner.list()
}

fun newCachedGlobalCoachingBaselineStore(inner: GlobalCoachingBaselineStore, ttl: Int, clock: Clock): GlobalCoachingBaselineStore {
    return CachedGlobalCoachingBaselineStore(
        inner = inner,
        cache = newTtlCache<GlobalCoachingBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global coaching baselines. Empty fields match all. */
data class GlobalCoachingBaselineQuery(
    var category: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCoachingBaseline): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCoachingBaseline>): MutableList<GlobalCoachingBaseline> {
        val matched = filter(items) { candidate: GlobalCoachingBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCoachingBaselines(store: GlobalCoachingBaselineStore, query: GlobalCoachingBaselineQuery): MutableList<GlobalCoachingBaseline> {
    return query.apply(store.list())
}
