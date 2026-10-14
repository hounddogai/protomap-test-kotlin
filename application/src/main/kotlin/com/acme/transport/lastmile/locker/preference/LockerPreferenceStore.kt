package com.acme.transport.lastmile.locker.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker preferences by code. */
interface LockerPreferenceStore {
    fun find(code: String): LockerPreference?
    fun save(item: LockerPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerPreferenceStore(
    var items: MutableMap<String, LockerPreference>,
    var order: MutableList<String>,
) : LockerPreferenceStore {
    override fun find(code: String): LockerPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerPreference) {
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

    override fun list(): MutableList<LockerPreference> {
        val out = mutableListOf<LockerPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerPreferenceStore(): LockerPreferenceStore {
    return MemoryLockerPreferenceStore(mutableMapOf<String, LockerPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerPreferenceStore(
    var inner: LockerPreferenceStore,
    var cache: TtlCache<LockerPreference>,
    var hits: Int,
    var misses: Int,
) : LockerPreferenceStore {
    override fun find(code: String): LockerPreference? {
        val hit = (cache.get(code) as LockerPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerPreference> = inner.list()
}

fun newCachedLockerPreferenceStore(inner: LockerPreferenceStore, ttl: Int, clock: Clock): LockerPreferenceStore {
    return CachedLockerPreferenceStore(
        inner = inner,
        cache = newTtlCache<LockerPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker preferences. Empty fields match all. */
data class LockerPreferenceQuery(
    var lane: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerPreference): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerPreference>): MutableList<LockerPreference> {
        val matched = filter(items) { candidate: LockerPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerPreferences(store: LockerPreferenceStore, query: LockerPreferenceQuery): MutableList<LockerPreference> {
    return query.apply(store.list())
}
