package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation preferences by code. */
interface SortationPreferenceStore {
    fun find(code: String): SortationPreference?
    fun save(item: SortationPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationPreferenceStore(
    var items: MutableMap<String, SortationPreference>,
    var order: MutableList<String>,
) : SortationPreferenceStore {
    override fun find(code: String): SortationPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationPreference) {
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

    override fun list(): MutableList<SortationPreference> {
        val out = mutableListOf<SortationPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationPreferenceStore(): SortationPreferenceStore {
    return MemorySortationPreferenceStore(mutableMapOf<String, SortationPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationPreferenceStore(
    var inner: SortationPreferenceStore,
    var cache: TtlCache<SortationPreference>,
    var hits: Int,
    var misses: Int,
) : SortationPreferenceStore {
    override fun find(code: String): SortationPreference? {
        val hit = (cache.get(code) as SortationPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationPreference> = inner.list()
}

fun newCachedSortationPreferenceStore(inner: SortationPreferenceStore, ttl: Int, clock: Clock): SortationPreferenceStore {
    return CachedSortationPreferenceStore(
        inner = inner,
        cache = newTtlCache<SortationPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation preferences. Empty fields match all. */
data class SortationPreferenceQuery(
    var channel: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationPreference): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<SortationPreference>): MutableList<SortationPreference> {
        val matched = filter(items) { candidate: SortationPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationPreferences(store: SortationPreferenceStore, query: SortationPreferenceQuery): MutableList<SortationPreference> {
    return query.apply(store.list())
}
