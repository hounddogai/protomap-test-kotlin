package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered backhaul preferences by code. */
interface TieredBackhaulPreferenceStore {
    fun find(code: String): TieredBackhaulPreference?
    fun save(item: TieredBackhaulPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredBackhaulPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredBackhaulPreferenceStore(
    var items: MutableMap<String, TieredBackhaulPreference>,
    var order: MutableList<String>,
) : TieredBackhaulPreferenceStore {
    override fun find(code: String): TieredBackhaulPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredBackhaulPreference) {
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

    override fun list(): MutableList<TieredBackhaulPreference> {
        val out = mutableListOf<TieredBackhaulPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredBackhaulPreferenceStore(): TieredBackhaulPreferenceStore {
    return MemoryTieredBackhaulPreferenceStore(
        mutableMapOf<String, TieredBackhaulPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredBackhaulPreferenceStore(
    var inner: TieredBackhaulPreferenceStore,
    var cache: TtlCache<TieredBackhaulPreference>,
    var hits: Int,
    var misses: Int,
) : TieredBackhaulPreferenceStore {
    override fun find(code: String): TieredBackhaulPreference? {
        val hit = (cache.get(code) as TieredBackhaulPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredBackhaulPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredBackhaulPreference> = inner.list()
}

fun newCachedTieredBackhaulPreferenceStore(inner: TieredBackhaulPreferenceStore, ttl: Int, clock: Clock): TieredBackhaulPreferenceStore {
    return CachedTieredBackhaulPreferenceStore(
        inner = inner,
        cache = newTtlCache<TieredBackhaulPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered backhaul preferences. Empty fields match all. */
data class TieredBackhaulPreferenceQuery(
    var channel: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredBackhaulPreference): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredBackhaulPreference>): MutableList<TieredBackhaulPreference> {
        val matched = filter(items) { candidate: TieredBackhaulPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredBackhaulPreferences(store: TieredBackhaulPreferenceStore, query: TieredBackhaulPreferenceQuery): MutableList<TieredBackhaulPreference> {
    return query.apply(store.list())
}
