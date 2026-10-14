package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity preferences by code. */
interface CapacityPreferenceStore {
    fun find(code: String): CapacityPreference?
    fun save(item: CapacityPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityPreferenceStore(
    var items: MutableMap<String, CapacityPreference>,
    var order: MutableList<String>,
) : CapacityPreferenceStore {
    override fun find(code: String): CapacityPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityPreference) {
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

    override fun list(): MutableList<CapacityPreference> {
        val out = mutableListOf<CapacityPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityPreferenceStore(): CapacityPreferenceStore {
    return MemoryCapacityPreferenceStore(mutableMapOf<String, CapacityPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityPreferenceStore(
    var inner: CapacityPreferenceStore,
    var cache: TtlCache<CapacityPreference>,
    var hits: Int,
    var misses: Int,
) : CapacityPreferenceStore {
    override fun find(code: String): CapacityPreference? {
        val hit = (cache.get(code) as CapacityPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityPreference> = inner.list()
}

fun newCachedCapacityPreferenceStore(inner: CapacityPreferenceStore, ttl: Int, clock: Clock): CapacityPreferenceStore {
    return CachedCapacityPreferenceStore(
        inner = inner,
        cache = newTtlCache<CapacityPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity preferences. Empty fields match all. */
data class CapacityPreferenceQuery(
    var category: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityPreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<CapacityPreference>): MutableList<CapacityPreference> {
        val matched = filter(items) { candidate: CapacityPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityPreferences(store: CapacityPreferenceStore, query: CapacityPreferenceQuery): MutableList<CapacityPreference> {
    return query.apply(store.list())
}
