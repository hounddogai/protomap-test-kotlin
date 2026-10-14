package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional redelivery preferences by code. */
interface RegionalRedeliveryPreferenceStore {
    fun find(code: String): RegionalRedeliveryPreference?
    fun save(item: RegionalRedeliveryPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRedeliveryPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRedeliveryPreferenceStore(
    var items: MutableMap<String, RegionalRedeliveryPreference>,
    var order: MutableList<String>,
) : RegionalRedeliveryPreferenceStore {
    override fun find(code: String): RegionalRedeliveryPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRedeliveryPreference) {
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

    override fun list(): MutableList<RegionalRedeliveryPreference> {
        val out = mutableListOf<RegionalRedeliveryPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRedeliveryPreferenceStore(): RegionalRedeliveryPreferenceStore {
    return MemoryRegionalRedeliveryPreferenceStore(
        mutableMapOf<String, RegionalRedeliveryPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRedeliveryPreferenceStore(
    var inner: RegionalRedeliveryPreferenceStore,
    var cache: TtlCache<RegionalRedeliveryPreference>,
    var hits: Int,
    var misses: Int,
) : RegionalRedeliveryPreferenceStore {
    override fun find(code: String): RegionalRedeliveryPreference? {
        val hit = (cache.get(code) as RegionalRedeliveryPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRedeliveryPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRedeliveryPreference> = inner.list()
}

fun newCachedRegionalRedeliveryPreferenceStore(inner: RegionalRedeliveryPreferenceStore, ttl: Int, clock: Clock): RegionalRedeliveryPreferenceStore {
    return CachedRegionalRedeliveryPreferenceStore(
        inner = inner,
        cache = newTtlCache<RegionalRedeliveryPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional redelivery preferences. Empty fields match all. */
data class RegionalRedeliveryPreferenceQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRedeliveryPreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalRedeliveryPreference>): MutableList<RegionalRedeliveryPreference> {
        val matched = filter(items) { candidate: RegionalRedeliveryPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRedeliveryPreferences(store: RegionalRedeliveryPreferenceStore, query: RegionalRedeliveryPreferenceQuery): MutableList<RegionalRedeliveryPreference> {
    return query.apply(store.list())
}
