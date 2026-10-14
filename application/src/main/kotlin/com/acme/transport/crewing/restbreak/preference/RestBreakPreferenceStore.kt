package com.acme.transport.crewing.restbreak.preference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break preferences by code. */
interface RestBreakPreferenceStore {
    fun find(code: String): RestBreakPreference?
    fun save(item: RestBreakPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakPreferenceStore(
    var items: MutableMap<String, RestBreakPreference>,
    var order: MutableList<String>,
) : RestBreakPreferenceStore {
    override fun find(code: String): RestBreakPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakPreference) {
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

    override fun list(): MutableList<RestBreakPreference> {
        val out = mutableListOf<RestBreakPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakPreferenceStore(): RestBreakPreferenceStore {
    return MemoryRestBreakPreferenceStore(mutableMapOf<String, RestBreakPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakPreferenceStore(
    var inner: RestBreakPreferenceStore,
    var cache: TtlCache<RestBreakPreference>,
    var hits: Int,
    var misses: Int,
) : RestBreakPreferenceStore {
    override fun find(code: String): RestBreakPreference? {
        val hit = (cache.get(code) as RestBreakPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakPreference> = inner.list()
}

fun newCachedRestBreakPreferenceStore(inner: RestBreakPreferenceStore, ttl: Int, clock: Clock): RestBreakPreferenceStore {
    return CachedRestBreakPreferenceStore(
        inner = inner,
        cache = newTtlCache<RestBreakPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break preferences. Empty fields match all. */
data class RestBreakPreferenceQuery(
    var source: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakPreference): Boolean {
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

    fun apply(items: MutableList<RestBreakPreference>): MutableList<RestBreakPreference> {
        val matched = filter(items) { candidate: RestBreakPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakPreferences(store: RestBreakPreferenceStore, query: RestBreakPreferenceQuery): MutableList<RestBreakPreference> {
    return query.apply(store.list())
}
