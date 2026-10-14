package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss preferences by code. */
interface NearMissPreferenceStore {
    fun find(code: String): NearMissPreference?
    fun save(item: NearMissPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissPreferenceStore(
    var items: MutableMap<String, NearMissPreference>,
    var order: MutableList<String>,
) : NearMissPreferenceStore {
    override fun find(code: String): NearMissPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissPreference) {
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

    override fun list(): MutableList<NearMissPreference> {
        val out = mutableListOf<NearMissPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissPreferenceStore(): NearMissPreferenceStore {
    return MemoryNearMissPreferenceStore(mutableMapOf<String, NearMissPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissPreferenceStore(
    var inner: NearMissPreferenceStore,
    var cache: TtlCache<NearMissPreference>,
    var hits: Int,
    var misses: Int,
) : NearMissPreferenceStore {
    override fun find(code: String): NearMissPreference? {
        val hit = (cache.get(code) as NearMissPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissPreference> = inner.list()
}

fun newCachedNearMissPreferenceStore(inner: NearMissPreferenceStore, ttl: Int, clock: Clock): NearMissPreferenceStore {
    return CachedNearMissPreferenceStore(
        inner = inner,
        cache = newTtlCache<NearMissPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss preferences. Empty fields match all. */
data class NearMissPreferenceQuery(
    var depot: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissPreference): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissPreference>): MutableList<NearMissPreference> {
        val matched = filter(items) { candidate: NearMissPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissPreferences(store: NearMissPreferenceStore, query: NearMissPreferenceQuery): MutableList<NearMissPreference> {
    return query.apply(store.list())
}
