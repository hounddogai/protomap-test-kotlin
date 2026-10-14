package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default dock door preferences by code. */
interface DefaultDockDoorPreferenceStore {
    fun find(code: String): DefaultDockDoorPreference?
    fun save(item: DefaultDockDoorPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDockDoorPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDockDoorPreferenceStore(
    var items: MutableMap<String, DefaultDockDoorPreference>,
    var order: MutableList<String>,
) : DefaultDockDoorPreferenceStore {
    override fun find(code: String): DefaultDockDoorPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDockDoorPreference) {
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

    override fun list(): MutableList<DefaultDockDoorPreference> {
        val out = mutableListOf<DefaultDockDoorPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDockDoorPreferenceStore(): DefaultDockDoorPreferenceStore {
    return MemoryDefaultDockDoorPreferenceStore(
        mutableMapOf<String, DefaultDockDoorPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDockDoorPreferenceStore(
    var inner: DefaultDockDoorPreferenceStore,
    var cache: TtlCache<DefaultDockDoorPreference>,
    var hits: Int,
    var misses: Int,
) : DefaultDockDoorPreferenceStore {
    override fun find(code: String): DefaultDockDoorPreference? {
        val hit = (cache.get(code) as DefaultDockDoorPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDockDoorPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDockDoorPreference> = inner.list()
}

fun newCachedDefaultDockDoorPreferenceStore(inner: DefaultDockDoorPreferenceStore, ttl: Int, clock: Clock): DefaultDockDoorPreferenceStore {
    return CachedDefaultDockDoorPreferenceStore(
        inner = inner,
        cache = newTtlCache<DefaultDockDoorPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default dock door preferences. Empty fields match all. */
data class DefaultDockDoorPreferenceQuery(
    var region: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDockDoorPreference): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<DefaultDockDoorPreference>): MutableList<DefaultDockDoorPreference> {
        val matched = filter(items) { candidate: DefaultDockDoorPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDockDoorPreferences(store: DefaultDockDoorPreferenceStore, query: DefaultDockDoorPreferenceQuery): MutableList<DefaultDockDoorPreference> {
    return query.apply(store.list())
}
