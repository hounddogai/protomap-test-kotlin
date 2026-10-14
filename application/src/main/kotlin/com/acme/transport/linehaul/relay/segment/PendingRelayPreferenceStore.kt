package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending relay preferences by code. */
interface PendingRelayPreferenceStore {
    fun find(code: String): PendingRelayPreference?
    fun save(item: PendingRelayPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRelayPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRelayPreferenceStore(
    var items: MutableMap<String, PendingRelayPreference>,
    var order: MutableList<String>,
) : PendingRelayPreferenceStore {
    override fun find(code: String): PendingRelayPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRelayPreference) {
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

    override fun list(): MutableList<PendingRelayPreference> {
        val out = mutableListOf<PendingRelayPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRelayPreferenceStore(): PendingRelayPreferenceStore {
    return MemoryPendingRelayPreferenceStore(
        mutableMapOf<String, PendingRelayPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRelayPreferenceStore(
    var inner: PendingRelayPreferenceStore,
    var cache: TtlCache<PendingRelayPreference>,
    var hits: Int,
    var misses: Int,
) : PendingRelayPreferenceStore {
    override fun find(code: String): PendingRelayPreference? {
        val hit = (cache.get(code) as PendingRelayPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRelayPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRelayPreference> = inner.list()
}

fun newCachedPendingRelayPreferenceStore(inner: PendingRelayPreferenceStore, ttl: Int, clock: Clock): PendingRelayPreferenceStore {
    return CachedPendingRelayPreferenceStore(
        inner = inner,
        cache = newTtlCache<PendingRelayPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending relay preferences. Empty fields match all. */
data class PendingRelayPreferenceQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRelayPreference): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRelayPreference>): MutableList<PendingRelayPreference> {
        val matched = filter(items) { candidate: PendingRelayPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRelayPreferences(store: PendingRelayPreferenceStore, query: PendingRelayPreferenceQuery): MutableList<PendingRelayPreference> {
    return query.apply(store.list())
}
