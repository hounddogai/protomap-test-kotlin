package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual locker preferences by code. */
interface ManualLockerPreferenceStore {
    fun find(code: String): ManualLockerPreference?
    fun save(item: ManualLockerPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualLockerPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualLockerPreferenceStore(
    var items: MutableMap<String, ManualLockerPreference>,
    var order: MutableList<String>,
) : ManualLockerPreferenceStore {
    override fun find(code: String): ManualLockerPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualLockerPreference) {
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

    override fun list(): MutableList<ManualLockerPreference> {
        val out = mutableListOf<ManualLockerPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualLockerPreferenceStore(): ManualLockerPreferenceStore {
    return MemoryManualLockerPreferenceStore(
        mutableMapOf<String, ManualLockerPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualLockerPreferenceStore(
    var inner: ManualLockerPreferenceStore,
    var cache: TtlCache<ManualLockerPreference>,
    var hits: Int,
    var misses: Int,
) : ManualLockerPreferenceStore {
    override fun find(code: String): ManualLockerPreference? {
        val hit = (cache.get(code) as ManualLockerPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualLockerPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualLockerPreference> = inner.list()
}

fun newCachedManualLockerPreferenceStore(inner: ManualLockerPreferenceStore, ttl: Int, clock: Clock): ManualLockerPreferenceStore {
    return CachedManualLockerPreferenceStore(
        inner = inner,
        cache = newTtlCache<ManualLockerPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual locker preferences. Empty fields match all. */
data class ManualLockerPreferenceQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualLockerPreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualLockerPreference>): MutableList<ManualLockerPreference> {
        val matched = filter(items) { candidate: ManualLockerPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualLockerPreferences(store: ManualLockerPreferenceStore, query: ManualLockerPreferenceQuery): MutableList<ManualLockerPreference> {
    return query.apply(store.list())
}
