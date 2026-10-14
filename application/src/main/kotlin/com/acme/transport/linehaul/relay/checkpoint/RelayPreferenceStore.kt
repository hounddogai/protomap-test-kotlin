package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay preferences by code. */
interface RelayPreferenceStore {
    fun find(code: String): RelayPreference?
    fun save(item: RelayPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayPreferenceStore(
    var items: MutableMap<String, RelayPreference>,
    var order: MutableList<String>,
) : RelayPreferenceStore {
    override fun find(code: String): RelayPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayPreference) {
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

    override fun list(): MutableList<RelayPreference> {
        val out = mutableListOf<RelayPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayPreferenceStore(): RelayPreferenceStore {
    return MemoryRelayPreferenceStore(mutableMapOf<String, RelayPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayPreferenceStore(
    var inner: RelayPreferenceStore,
    var cache: TtlCache<RelayPreference>,
    var hits: Int,
    var misses: Int,
) : RelayPreferenceStore {
    override fun find(code: String): RelayPreference? {
        val hit = (cache.get(code) as RelayPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayPreference> = inner.list()
}

fun newCachedRelayPreferenceStore(inner: RelayPreferenceStore, ttl: Int, clock: Clock): RelayPreferenceStore {
    return CachedRelayPreferenceStore(
        inner = inner,
        cache = newTtlCache<RelayPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay preferences. Empty fields match all. */
data class RelayPreferenceQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayPreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayPreference>): MutableList<RelayPreference> {
        val matched = filter(items) { candidate: RelayPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayPreferences(store: RelayPreferenceStore, query: RelayPreferenceQuery): MutableList<RelayPreference> {
    return query.apply(store.list())
}
