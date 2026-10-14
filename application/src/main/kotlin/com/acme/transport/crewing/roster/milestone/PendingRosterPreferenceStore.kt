package com.acme.transport.crewing.roster.milestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending roster preferences by code. */
interface PendingRosterPreferenceStore {
    fun find(code: String): PendingRosterPreference?
    fun save(item: PendingRosterPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRosterPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRosterPreferenceStore(
    var items: MutableMap<String, PendingRosterPreference>,
    var order: MutableList<String>,
) : PendingRosterPreferenceStore {
    override fun find(code: String): PendingRosterPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRosterPreference) {
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

    override fun list(): MutableList<PendingRosterPreference> {
        val out = mutableListOf<PendingRosterPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRosterPreferenceStore(): PendingRosterPreferenceStore {
    return MemoryPendingRosterPreferenceStore(
        mutableMapOf<String, PendingRosterPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRosterPreferenceStore(
    var inner: PendingRosterPreferenceStore,
    var cache: TtlCache<PendingRosterPreference>,
    var hits: Int,
    var misses: Int,
) : PendingRosterPreferenceStore {
    override fun find(code: String): PendingRosterPreference? {
        val hit = (cache.get(code) as PendingRosterPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRosterPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRosterPreference> = inner.list()
}

fun newCachedPendingRosterPreferenceStore(inner: PendingRosterPreferenceStore, ttl: Int, clock: Clock): PendingRosterPreferenceStore {
    return CachedPendingRosterPreferenceStore(
        inner = inner,
        cache = newTtlCache<PendingRosterPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending roster preferences. Empty fields match all. */
data class PendingRosterPreferenceQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRosterPreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRosterPreference>): MutableList<PendingRosterPreference> {
        val matched = filter(items) { candidate: PendingRosterPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRosterPreferences(store: PendingRosterPreferenceStore, query: PendingRosterPreferenceQuery): MutableList<PendingRosterPreference> {
    return query.apply(store.list())
}
