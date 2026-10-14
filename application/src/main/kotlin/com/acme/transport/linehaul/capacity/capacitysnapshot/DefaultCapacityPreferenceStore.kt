package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default capacity preferences by code. */
interface DefaultCapacityPreferenceStore {
    fun find(code: String): DefaultCapacityPreference?
    fun save(item: DefaultCapacityPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCapacityPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCapacityPreferenceStore(
    var items: MutableMap<String, DefaultCapacityPreference>,
    var order: MutableList<String>,
) : DefaultCapacityPreferenceStore {
    override fun find(code: String): DefaultCapacityPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCapacityPreference) {
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

    override fun list(): MutableList<DefaultCapacityPreference> {
        val out = mutableListOf<DefaultCapacityPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCapacityPreferenceStore(): DefaultCapacityPreferenceStore {
    return MemoryDefaultCapacityPreferenceStore(
        mutableMapOf<String, DefaultCapacityPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCapacityPreferenceStore(
    var inner: DefaultCapacityPreferenceStore,
    var cache: TtlCache<DefaultCapacityPreference>,
    var hits: Int,
    var misses: Int,
) : DefaultCapacityPreferenceStore {
    override fun find(code: String): DefaultCapacityPreference? {
        val hit = (cache.get(code) as DefaultCapacityPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCapacityPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCapacityPreference> = inner.list()
}

fun newCachedDefaultCapacityPreferenceStore(inner: DefaultCapacityPreferenceStore, ttl: Int, clock: Clock): DefaultCapacityPreferenceStore {
    return CachedDefaultCapacityPreferenceStore(
        inner = inner,
        cache = newTtlCache<DefaultCapacityPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default capacity preferences. Empty fields match all. */
data class DefaultCapacityPreferenceQuery(
    var country: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCapacityPreference): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCapacityPreference>): MutableList<DefaultCapacityPreference> {
        val matched = filter(items) { candidate: DefaultCapacityPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCapacityPreferences(store: DefaultCapacityPreferenceStore, query: DefaultCapacityPreferenceQuery): MutableList<DefaultCapacityPreference> {
    return query.apply(store.list())
}
