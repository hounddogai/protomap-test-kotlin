package com.acme.transport.emissions.idling.score

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling preferences by code. */
interface IdlingPreferenceStore {
    fun find(code: String): IdlingPreference?
    fun save(item: IdlingPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingPreferenceStore(
    var items: MutableMap<String, IdlingPreference>,
    var order: MutableList<String>,
) : IdlingPreferenceStore {
    override fun find(code: String): IdlingPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingPreference) {
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

    override fun list(): MutableList<IdlingPreference> {
        val out = mutableListOf<IdlingPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingPreferenceStore(): IdlingPreferenceStore {
    return MemoryIdlingPreferenceStore(mutableMapOf<String, IdlingPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingPreferenceStore(
    var inner: IdlingPreferenceStore,
    var cache: TtlCache<IdlingPreference>,
    var hits: Int,
    var misses: Int,
) : IdlingPreferenceStore {
    override fun find(code: String): IdlingPreference? {
        val hit = (cache.get(code) as IdlingPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingPreference> = inner.list()
}

fun newCachedIdlingPreferenceStore(inner: IdlingPreferenceStore, ttl: Int, clock: Clock): IdlingPreferenceStore {
    return CachedIdlingPreferenceStore(
        inner = inner,
        cache = newTtlCache<IdlingPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling preferences. Empty fields match all. */
data class IdlingPreferenceQuery(
    var locale: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingPreference): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingPreference>): MutableList<IdlingPreference> {
        val matched = filter(items) { candidate: IdlingPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingPreferences(store: IdlingPreferenceStore, query: IdlingPreferenceQuery): MutableList<IdlingPreference> {
    return query.apply(store.list())
}
