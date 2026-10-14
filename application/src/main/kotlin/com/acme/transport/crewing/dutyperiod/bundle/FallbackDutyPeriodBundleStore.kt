package com.acme.transport.crewing.dutyperiod.bundle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback duty period bundles by code. */
interface FallbackDutyPeriodBundleStore {
    fun find(code: String): FallbackDutyPeriodBundle?
    fun save(item: FallbackDutyPeriodBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDutyPeriodBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDutyPeriodBundleStore(
    var items: MutableMap<String, FallbackDutyPeriodBundle>,
    var order: MutableList<String>,
) : FallbackDutyPeriodBundleStore {
    override fun find(code: String): FallbackDutyPeriodBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDutyPeriodBundle) {
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

    override fun list(): MutableList<FallbackDutyPeriodBundle> {
        val out = mutableListOf<FallbackDutyPeriodBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDutyPeriodBundleStore(): FallbackDutyPeriodBundleStore {
    return MemoryFallbackDutyPeriodBundleStore(
        mutableMapOf<String, FallbackDutyPeriodBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDutyPeriodBundleStore(
    var inner: FallbackDutyPeriodBundleStore,
    var cache: TtlCache<FallbackDutyPeriodBundle>,
    var hits: Int,
    var misses: Int,
) : FallbackDutyPeriodBundleStore {
    override fun find(code: String): FallbackDutyPeriodBundle? {
        val hit = (cache.get(code) as FallbackDutyPeriodBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDutyPeriodBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDutyPeriodBundle> = inner.list()
}

fun newCachedFallbackDutyPeriodBundleStore(inner: FallbackDutyPeriodBundleStore, ttl: Int, clock: Clock): FallbackDutyPeriodBundleStore {
    return CachedFallbackDutyPeriodBundleStore(
        inner = inner,
        cache = newTtlCache<FallbackDutyPeriodBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback duty period bundles. Empty fields match all. */
data class FallbackDutyPeriodBundleQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDutyPeriodBundle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDutyPeriodBundle>): MutableList<FallbackDutyPeriodBundle> {
        val matched = filter(items) { candidate: FallbackDutyPeriodBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDutyPeriodBundles(store: FallbackDutyPeriodBundleStore, query: FallbackDutyPeriodBundleQuery): MutableList<FallbackDutyPeriodBundle> {
    return query.apply(store.list())
}
