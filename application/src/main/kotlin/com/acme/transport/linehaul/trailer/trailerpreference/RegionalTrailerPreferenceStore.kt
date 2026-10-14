package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional trailer preferences by code. */
interface RegionalTrailerPreferenceStore {
    fun find(code: String): RegionalTrailerPreference?
    fun save(item: RegionalTrailerPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTrailerPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTrailerPreferenceStore(
    var items: MutableMap<String, RegionalTrailerPreference>,
    var order: MutableList<String>,
) : RegionalTrailerPreferenceStore {
    override fun find(code: String): RegionalTrailerPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTrailerPreference) {
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

    override fun list(): MutableList<RegionalTrailerPreference> {
        val out = mutableListOf<RegionalTrailerPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTrailerPreferenceStore(): RegionalTrailerPreferenceStore {
    return MemoryRegionalTrailerPreferenceStore(
        mutableMapOf<String, RegionalTrailerPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTrailerPreferenceStore(
    var inner: RegionalTrailerPreferenceStore,
    var cache: TtlCache<RegionalTrailerPreference>,
    var hits: Int,
    var misses: Int,
) : RegionalTrailerPreferenceStore {
    override fun find(code: String): RegionalTrailerPreference? {
        val hit = (cache.get(code) as RegionalTrailerPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTrailerPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTrailerPreference> = inner.list()
}

fun newCachedRegionalTrailerPreferenceStore(inner: RegionalTrailerPreferenceStore, ttl: Int, clock: Clock): RegionalTrailerPreferenceStore {
    return CachedRegionalTrailerPreferenceStore(
        inner = inner,
        cache = newTtlCache<RegionalTrailerPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional trailer preferences. Empty fields match all. */
data class RegionalTrailerPreferenceQuery(
    var region: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTrailerPreference): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<RegionalTrailerPreference>): MutableList<RegionalTrailerPreference> {
        val matched = filter(items) { candidate: RegionalTrailerPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTrailerPreferences(store: RegionalTrailerPreferenceStore, query: RegionalTrailerPreferenceQuery): MutableList<RegionalTrailerPreference> {
    return query.apply(store.list())
}
