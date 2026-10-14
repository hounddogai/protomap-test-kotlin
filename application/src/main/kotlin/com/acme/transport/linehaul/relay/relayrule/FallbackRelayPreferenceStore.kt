package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback relay preferences by code. */
interface FallbackRelayPreferenceStore {
    fun find(code: String): FallbackRelayPreference?
    fun save(item: FallbackRelayPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRelayPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRelayPreferenceStore(
    var items: MutableMap<String, FallbackRelayPreference>,
    var order: MutableList<String>,
) : FallbackRelayPreferenceStore {
    override fun find(code: String): FallbackRelayPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRelayPreference) {
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

    override fun list(): MutableList<FallbackRelayPreference> {
        val out = mutableListOf<FallbackRelayPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRelayPreferenceStore(): FallbackRelayPreferenceStore {
    return MemoryFallbackRelayPreferenceStore(
        mutableMapOf<String, FallbackRelayPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRelayPreferenceStore(
    var inner: FallbackRelayPreferenceStore,
    var cache: TtlCache<FallbackRelayPreference>,
    var hits: Int,
    var misses: Int,
) : FallbackRelayPreferenceStore {
    override fun find(code: String): FallbackRelayPreference? {
        val hit = (cache.get(code) as FallbackRelayPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRelayPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRelayPreference> = inner.list()
}

fun newCachedFallbackRelayPreferenceStore(inner: FallbackRelayPreferenceStore, ttl: Int, clock: Clock): FallbackRelayPreferenceStore {
    return CachedFallbackRelayPreferenceStore(
        inner = inner,
        cache = newTtlCache<FallbackRelayPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback relay preferences. Empty fields match all. */
data class FallbackRelayPreferenceQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRelayPreference): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRelayPreference>): MutableList<FallbackRelayPreference> {
        val matched = filter(items) { candidate: FallbackRelayPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRelayPreferences(store: FallbackRelayPreferenceStore, query: FallbackRelayPreferenceQuery): MutableList<FallbackRelayPreference> {
    return query.apply(store.list())
}
