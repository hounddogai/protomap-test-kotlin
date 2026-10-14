package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell preferences by code. */
interface DwellPreferenceStore {
    fun find(code: String): DwellPreference?
    fun save(item: DwellPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellPreferenceStore(
    var items: MutableMap<String, DwellPreference>,
    var order: MutableList<String>,
) : DwellPreferenceStore {
    override fun find(code: String): DwellPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellPreference) {
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

    override fun list(): MutableList<DwellPreference> {
        val out = mutableListOf<DwellPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellPreferenceStore(): DwellPreferenceStore {
    return MemoryDwellPreferenceStore(mutableMapOf<String, DwellPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellPreferenceStore(
    var inner: DwellPreferenceStore,
    var cache: TtlCache<DwellPreference>,
    var hits: Int,
    var misses: Int,
) : DwellPreferenceStore {
    override fun find(code: String): DwellPreference? {
        val hit = (cache.get(code) as DwellPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellPreference> = inner.list()
}

fun newCachedDwellPreferenceStore(inner: DwellPreferenceStore, ttl: Int, clock: Clock): DwellPreferenceStore {
    return CachedDwellPreferenceStore(
        inner = inner,
        cache = newTtlCache<DwellPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell preferences. Empty fields match all. */
data class DwellPreferenceQuery(
    var segment: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellPreference): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellPreference>): MutableList<DwellPreference> {
        val matched = filter(items) { candidate: DwellPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellPreferences(store: DwellPreferenceStore, query: DwellPreferenceQuery): MutableList<DwellPreference> {
    return query.apply(store.list())
}
