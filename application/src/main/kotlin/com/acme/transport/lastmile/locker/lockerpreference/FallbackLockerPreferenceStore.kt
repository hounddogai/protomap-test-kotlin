package com.acme.transport.lastmile.locker.lockerpreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback locker preferences by code. */
interface FallbackLockerPreferenceStore {
    fun find(code: String): FallbackLockerPreference?
    fun save(item: FallbackLockerPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackLockerPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackLockerPreferenceStore(
    var items: MutableMap<String, FallbackLockerPreference>,
    var order: MutableList<String>,
) : FallbackLockerPreferenceStore {
    override fun find(code: String): FallbackLockerPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackLockerPreference) {
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

    override fun list(): MutableList<FallbackLockerPreference> {
        val out = mutableListOf<FallbackLockerPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackLockerPreferenceStore(): FallbackLockerPreferenceStore {
    return MemoryFallbackLockerPreferenceStore(
        mutableMapOf<String, FallbackLockerPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackLockerPreferenceStore(
    var inner: FallbackLockerPreferenceStore,
    var cache: TtlCache<FallbackLockerPreference>,
    var hits: Int,
    var misses: Int,
) : FallbackLockerPreferenceStore {
    override fun find(code: String): FallbackLockerPreference? {
        val hit = (cache.get(code) as FallbackLockerPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackLockerPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackLockerPreference> = inner.list()
}

fun newCachedFallbackLockerPreferenceStore(inner: FallbackLockerPreferenceStore, ttl: Int, clock: Clock): FallbackLockerPreferenceStore {
    return CachedFallbackLockerPreferenceStore(
        inner = inner,
        cache = newTtlCache<FallbackLockerPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback locker preferences. Empty fields match all. */
data class FallbackLockerPreferenceQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackLockerPreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackLockerPreference>): MutableList<FallbackLockerPreference> {
        val matched = filter(items) { candidate: FallbackLockerPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackLockerPreferences(store: FallbackLockerPreferenceStore, query: FallbackLockerPreferenceQuery): MutableList<FallbackLockerPreference> {
    return query.apply(store.list())
}
