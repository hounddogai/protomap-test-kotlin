package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period bundles by code. */
interface DutyPeriodBundleStore {
    fun find(code: String): DutyPeriodBundle?
    fun save(item: DutyPeriodBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodBundleStore(
    var items: MutableMap<String, DutyPeriodBundle>,
    var order: MutableList<String>,
) : DutyPeriodBundleStore {
    override fun find(code: String): DutyPeriodBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodBundle) {
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

    override fun list(): MutableList<DutyPeriodBundle> {
        val out = mutableListOf<DutyPeriodBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodBundleStore(): DutyPeriodBundleStore {
    return MemoryDutyPeriodBundleStore(mutableMapOf<String, DutyPeriodBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodBundleStore(
    var inner: DutyPeriodBundleStore,
    var cache: TtlCache<DutyPeriodBundle>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodBundleStore {
    override fun find(code: String): DutyPeriodBundle? {
        val hit = (cache.get(code) as DutyPeriodBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodBundle> = inner.list()
}

fun newCachedDutyPeriodBundleStore(inner: DutyPeriodBundleStore, ttl: Int, clock: Clock): DutyPeriodBundleStore {
    return CachedDutyPeriodBundleStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period bundles. Empty fields match all. */
data class DutyPeriodBundleQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodBundle): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodBundle>): MutableList<DutyPeriodBundle> {
        val matched = filter(items) { candidate: DutyPeriodBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodBundles(store: DutyPeriodBundleStore, query: DutyPeriodBundleQuery): MutableList<DutyPeriodBundle> {
    return query.apply(store.list())
}
