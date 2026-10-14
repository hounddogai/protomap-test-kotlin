package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff preferences by code. */
interface CutoffPreferenceStore {
    fun find(code: String): CutoffPreference?
    fun save(item: CutoffPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffPreferenceStore(
    var items: MutableMap<String, CutoffPreference>,
    var order: MutableList<String>,
) : CutoffPreferenceStore {
    override fun find(code: String): CutoffPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffPreference) {
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

    override fun list(): MutableList<CutoffPreference> {
        val out = mutableListOf<CutoffPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffPreferenceStore(): CutoffPreferenceStore {
    return MemoryCutoffPreferenceStore(mutableMapOf<String, CutoffPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffPreferenceStore(
    var inner: CutoffPreferenceStore,
    var cache: TtlCache<CutoffPreference>,
    var hits: Int,
    var misses: Int,
) : CutoffPreferenceStore {
    override fun find(code: String): CutoffPreference? {
        val hit = (cache.get(code) as CutoffPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffPreference> = inner.list()
}

fun newCachedCutoffPreferenceStore(inner: CutoffPreferenceStore, ttl: Int, clock: Clock): CutoffPreferenceStore {
    return CachedCutoffPreferenceStore(
        inner = inner,
        cache = newTtlCache<CutoffPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff preferences. Empty fields match all. */
data class CutoffPreferenceQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffPreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffPreference>): MutableList<CutoffPreference> {
        val matched = filter(items) { candidate: CutoffPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffPreferences(store: CutoffPreferenceStore, query: CutoffPreferenceQuery): MutableList<CutoffPreference> {
    return query.apply(store.list())
}
