package com.acme.transport.terminal.sortation.sortationpreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling sortation preferences by code. */
interface RollingSortationPreferenceStore {
    fun find(code: String): RollingSortationPreference?
    fun save(item: RollingSortationPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingSortationPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingSortationPreferenceStore(
    var items: MutableMap<String, RollingSortationPreference>,
    var order: MutableList<String>,
) : RollingSortationPreferenceStore {
    override fun find(code: String): RollingSortationPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingSortationPreference) {
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

    override fun list(): MutableList<RollingSortationPreference> {
        val out = mutableListOf<RollingSortationPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingSortationPreferenceStore(): RollingSortationPreferenceStore {
    return MemoryRollingSortationPreferenceStore(
        mutableMapOf<String, RollingSortationPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingSortationPreferenceStore(
    var inner: RollingSortationPreferenceStore,
    var cache: TtlCache<RollingSortationPreference>,
    var hits: Int,
    var misses: Int,
) : RollingSortationPreferenceStore {
    override fun find(code: String): RollingSortationPreference? {
        val hit = (cache.get(code) as RollingSortationPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingSortationPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingSortationPreference> = inner.list()
}

fun newCachedRollingSortationPreferenceStore(inner: RollingSortationPreferenceStore, ttl: Int, clock: Clock): RollingSortationPreferenceStore {
    return CachedRollingSortationPreferenceStore(
        inner = inner,
        cache = newTtlCache<RollingSortationPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling sortation preferences. Empty fields match all. */
data class RollingSortationPreferenceQuery(
    var channel: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingSortationPreference): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingSortationPreference>): MutableList<RollingSortationPreference> {
        val matched = filter(items) { candidate: RollingSortationPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingSortationPreferences(store: RollingSortationPreferenceStore, query: RollingSortationPreferenceQuery): MutableList<RollingSortationPreference> {
    return query.apply(store.list())
}
