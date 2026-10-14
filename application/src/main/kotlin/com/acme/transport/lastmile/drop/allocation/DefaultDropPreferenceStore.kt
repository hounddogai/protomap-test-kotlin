package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default drop preferences by code. */
interface DefaultDropPreferenceStore {
    fun find(code: String): DefaultDropPreference?
    fun save(item: DefaultDropPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDropPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDropPreferenceStore(
    var items: MutableMap<String, DefaultDropPreference>,
    var order: MutableList<String>,
) : DefaultDropPreferenceStore {
    override fun find(code: String): DefaultDropPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDropPreference) {
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

    override fun list(): MutableList<DefaultDropPreference> {
        val out = mutableListOf<DefaultDropPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDropPreferenceStore(): DefaultDropPreferenceStore {
    return MemoryDefaultDropPreferenceStore(
        mutableMapOf<String, DefaultDropPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDropPreferenceStore(
    var inner: DefaultDropPreferenceStore,
    var cache: TtlCache<DefaultDropPreference>,
    var hits: Int,
    var misses: Int,
) : DefaultDropPreferenceStore {
    override fun find(code: String): DefaultDropPreference? {
        val hit = (cache.get(code) as DefaultDropPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDropPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDropPreference> = inner.list()
}

fun newCachedDefaultDropPreferenceStore(inner: DefaultDropPreferenceStore, ttl: Int, clock: Clock): DefaultDropPreferenceStore {
    return CachedDefaultDropPreferenceStore(
        inner = inner,
        cache = newTtlCache<DefaultDropPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default drop preferences. Empty fields match all. */
data class DefaultDropPreferenceQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDropPreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDropPreference>): MutableList<DefaultDropPreference> {
        val matched = filter(items) { candidate: DefaultDropPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDropPreferences(store: DefaultDropPreferenceStore, query: DefaultDropPreferenceQuery): MutableList<DefaultDropPreference> {
    return query.apply(store.list())
}
