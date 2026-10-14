package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default cross dock preferences by code. */
interface DefaultCrossDockPreferenceStore {
    fun find(code: String): DefaultCrossDockPreference?
    fun save(item: DefaultCrossDockPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCrossDockPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCrossDockPreferenceStore(
    var items: MutableMap<String, DefaultCrossDockPreference>,
    var order: MutableList<String>,
) : DefaultCrossDockPreferenceStore {
    override fun find(code: String): DefaultCrossDockPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCrossDockPreference) {
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

    override fun list(): MutableList<DefaultCrossDockPreference> {
        val out = mutableListOf<DefaultCrossDockPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCrossDockPreferenceStore(): DefaultCrossDockPreferenceStore {
    return MemoryDefaultCrossDockPreferenceStore(
        mutableMapOf<String, DefaultCrossDockPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCrossDockPreferenceStore(
    var inner: DefaultCrossDockPreferenceStore,
    var cache: TtlCache<DefaultCrossDockPreference>,
    var hits: Int,
    var misses: Int,
) : DefaultCrossDockPreferenceStore {
    override fun find(code: String): DefaultCrossDockPreference? {
        val hit = (cache.get(code) as DefaultCrossDockPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCrossDockPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCrossDockPreference> = inner.list()
}

fun newCachedDefaultCrossDockPreferenceStore(inner: DefaultCrossDockPreferenceStore, ttl: Int, clock: Clock): DefaultCrossDockPreferenceStore {
    return CachedDefaultCrossDockPreferenceStore(
        inner = inner,
        cache = newTtlCache<DefaultCrossDockPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default cross dock preferences. Empty fields match all. */
data class DefaultCrossDockPreferenceQuery(
    var category: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCrossDockPreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<DefaultCrossDockPreference>): MutableList<DefaultCrossDockPreference> {
        val matched = filter(items) { candidate: DefaultCrossDockPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCrossDockPreferences(store: DefaultCrossDockPreferenceStore, query: DefaultCrossDockPreferenceQuery): MutableList<DefaultCrossDockPreference> {
    return query.apply(store.list())
}
