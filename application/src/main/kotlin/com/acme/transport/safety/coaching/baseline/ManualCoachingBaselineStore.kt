package com.acme.transport.safety.coaching.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual coaching baselines by code. */
interface ManualCoachingBaselineStore {
    fun find(code: String): ManualCoachingBaseline?
    fun save(item: ManualCoachingBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCoachingBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCoachingBaselineStore(
    var items: MutableMap<String, ManualCoachingBaseline>,
    var order: MutableList<String>,
) : ManualCoachingBaselineStore {
    override fun find(code: String): ManualCoachingBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCoachingBaseline) {
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

    override fun list(): MutableList<ManualCoachingBaseline> {
        val out = mutableListOf<ManualCoachingBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCoachingBaselineStore(): ManualCoachingBaselineStore {
    return MemoryManualCoachingBaselineStore(
        mutableMapOf<String, ManualCoachingBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCoachingBaselineStore(
    var inner: ManualCoachingBaselineStore,
    var cache: TtlCache<ManualCoachingBaseline>,
    var hits: Int,
    var misses: Int,
) : ManualCoachingBaselineStore {
    override fun find(code: String): ManualCoachingBaseline? {
        val hit = (cache.get(code) as ManualCoachingBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCoachingBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCoachingBaseline> = inner.list()
}

fun newCachedManualCoachingBaselineStore(inner: ManualCoachingBaselineStore, ttl: Int, clock: Clock): ManualCoachingBaselineStore {
    return CachedManualCoachingBaselineStore(
        inner = inner,
        cache = newTtlCache<ManualCoachingBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual coaching baselines. Empty fields match all. */
data class ManualCoachingBaselineQuery(
    var source: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCoachingBaseline): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCoachingBaseline>): MutableList<ManualCoachingBaseline> {
        val matched = filter(items) { candidate: ManualCoachingBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCoachingBaselines(store: ManualCoachingBaselineStore, query: ManualCoachingBaselineQuery): MutableList<ManualCoachingBaseline> {
    return query.apply(store.list())
}
