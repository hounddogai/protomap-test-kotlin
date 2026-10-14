package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default dwell preferences by code. */
interface DefaultDwellPreferenceStore {
    fun find(code: String): DefaultDwellPreference?
    fun save(item: DefaultDwellPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDwellPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDwellPreferenceStore(
    var items: MutableMap<String, DefaultDwellPreference>,
    var order: MutableList<String>,
) : DefaultDwellPreferenceStore {
    override fun find(code: String): DefaultDwellPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDwellPreference) {
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

    override fun list(): MutableList<DefaultDwellPreference> {
        val out = mutableListOf<DefaultDwellPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDwellPreferenceStore(): DefaultDwellPreferenceStore {
    return MemoryDefaultDwellPreferenceStore(
        mutableMapOf<String, DefaultDwellPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDwellPreferenceStore(
    var inner: DefaultDwellPreferenceStore,
    var cache: TtlCache<DefaultDwellPreference>,
    var hits: Int,
    var misses: Int,
) : DefaultDwellPreferenceStore {
    override fun find(code: String): DefaultDwellPreference? {
        val hit = (cache.get(code) as DefaultDwellPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDwellPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDwellPreference> = inner.list()
}

fun newCachedDefaultDwellPreferenceStore(inner: DefaultDwellPreferenceStore, ttl: Int, clock: Clock): DefaultDwellPreferenceStore {
    return CachedDefaultDwellPreferenceStore(
        inner = inner,
        cache = newTtlCache<DefaultDwellPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default dwell preferences. Empty fields match all. */
data class DefaultDwellPreferenceQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDwellPreference): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDwellPreference>): MutableList<DefaultDwellPreference> {
        val matched = filter(items) { candidate: DefaultDwellPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDwellPreferences(store: DefaultDwellPreferenceStore, query: DefaultDwellPreferenceQuery): MutableList<DefaultDwellPreference> {
    return query.apply(store.list())
}
