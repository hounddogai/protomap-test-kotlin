package com.acme.transport.crewing.roster.preference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default roster preferences by code. */
interface DefaultRosterPreferenceStore {
    fun find(code: String): DefaultRosterPreference?
    fun save(item: DefaultRosterPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRosterPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRosterPreferenceStore(
    var items: MutableMap<String, DefaultRosterPreference>,
    var order: MutableList<String>,
) : DefaultRosterPreferenceStore {
    override fun find(code: String): DefaultRosterPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRosterPreference) {
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

    override fun list(): MutableList<DefaultRosterPreference> {
        val out = mutableListOf<DefaultRosterPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRosterPreferenceStore(): DefaultRosterPreferenceStore {
    return MemoryDefaultRosterPreferenceStore(
        mutableMapOf<String, DefaultRosterPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRosterPreferenceStore(
    var inner: DefaultRosterPreferenceStore,
    var cache: TtlCache<DefaultRosterPreference>,
    var hits: Int,
    var misses: Int,
) : DefaultRosterPreferenceStore {
    override fun find(code: String): DefaultRosterPreference? {
        val hit = (cache.get(code) as DefaultRosterPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRosterPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRosterPreference> = inner.list()
}

fun newCachedDefaultRosterPreferenceStore(inner: DefaultRosterPreferenceStore, ttl: Int, clock: Clock): DefaultRosterPreferenceStore {
    return CachedDefaultRosterPreferenceStore(
        inner = inner,
        cache = newTtlCache<DefaultRosterPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default roster preferences. Empty fields match all. */
data class DefaultRosterPreferenceQuery(
    var category: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRosterPreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRosterPreference>): MutableList<DefaultRosterPreference> {
        val matched = filter(items) { candidate: DefaultRosterPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRosterPreferences(store: DefaultRosterPreferenceStore, query: DefaultRosterPreferenceQuery): MutableList<DefaultRosterPreference> {
    return query.apply(store.list())
}
