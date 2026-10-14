package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation preferences by code. */
interface ViolationPreferenceStore {
    fun find(code: String): ViolationPreference?
    fun save(item: ViolationPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationPreferenceStore(
    var items: MutableMap<String, ViolationPreference>,
    var order: MutableList<String>,
) : ViolationPreferenceStore {
    override fun find(code: String): ViolationPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationPreference) {
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

    override fun list(): MutableList<ViolationPreference> {
        val out = mutableListOf<ViolationPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationPreferenceStore(): ViolationPreferenceStore {
    return MemoryViolationPreferenceStore(mutableMapOf<String, ViolationPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationPreferenceStore(
    var inner: ViolationPreferenceStore,
    var cache: TtlCache<ViolationPreference>,
    var hits: Int,
    var misses: Int,
) : ViolationPreferenceStore {
    override fun find(code: String): ViolationPreference? {
        val hit = (cache.get(code) as ViolationPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationPreference> = inner.list()
}

fun newCachedViolationPreferenceStore(inner: ViolationPreferenceStore, ttl: Int, clock: Clock): ViolationPreferenceStore {
    return CachedViolationPreferenceStore(
        inner = inner,
        cache = newTtlCache<ViolationPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation preferences. Empty fields match all. */
data class ViolationPreferenceQuery(
    var segment: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationPreference): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationPreference>): MutableList<ViolationPreference> {
        val matched = filter(items) { candidate: ViolationPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationPreferences(store: ViolationPreferenceStore, query: ViolationPreferenceQuery): MutableList<ViolationPreference> {
    return query.apply(store.list())
}
