package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional drop preferences by code. */
interface RegionalDropPreferenceStore {
    fun find(code: String): RegionalDropPreference?
    fun save(item: RegionalDropPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDropPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDropPreferenceStore(
    var items: MutableMap<String, RegionalDropPreference>,
    var order: MutableList<String>,
) : RegionalDropPreferenceStore {
    override fun find(code: String): RegionalDropPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDropPreference) {
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

    override fun list(): MutableList<RegionalDropPreference> {
        val out = mutableListOf<RegionalDropPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDropPreferenceStore(): RegionalDropPreferenceStore {
    return MemoryRegionalDropPreferenceStore(
        mutableMapOf<String, RegionalDropPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDropPreferenceStore(
    var inner: RegionalDropPreferenceStore,
    var cache: TtlCache<RegionalDropPreference>,
    var hits: Int,
    var misses: Int,
) : RegionalDropPreferenceStore {
    override fun find(code: String): RegionalDropPreference? {
        val hit = (cache.get(code) as RegionalDropPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDropPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDropPreference> = inner.list()
}

fun newCachedRegionalDropPreferenceStore(inner: RegionalDropPreferenceStore, ttl: Int, clock: Clock): RegionalDropPreferenceStore {
    return CachedRegionalDropPreferenceStore(
        inner = inner,
        cache = newTtlCache<RegionalDropPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional drop preferences. Empty fields match all. */
data class RegionalDropPreferenceQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDropPreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDropPreference>): MutableList<RegionalDropPreference> {
        val matched = filter(items) { candidate: RegionalDropPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDropPreferences(store: RegionalDropPreferenceStore, query: RegionalDropPreferenceQuery): MutableList<RegionalDropPreference> {
    return query.apply(store.list())
}
