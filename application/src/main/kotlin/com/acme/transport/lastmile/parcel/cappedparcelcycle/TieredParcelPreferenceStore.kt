package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered parcel preferences by code. */
interface TieredParcelPreferenceStore {
    fun find(code: String): TieredParcelPreference?
    fun save(item: TieredParcelPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredParcelPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredParcelPreferenceStore(
    var items: MutableMap<String, TieredParcelPreference>,
    var order: MutableList<String>,
) : TieredParcelPreferenceStore {
    override fun find(code: String): TieredParcelPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredParcelPreference) {
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

    override fun list(): MutableList<TieredParcelPreference> {
        val out = mutableListOf<TieredParcelPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredParcelPreferenceStore(): TieredParcelPreferenceStore {
    return MemoryTieredParcelPreferenceStore(
        mutableMapOf<String, TieredParcelPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredParcelPreferenceStore(
    var inner: TieredParcelPreferenceStore,
    var cache: TtlCache<TieredParcelPreference>,
    var hits: Int,
    var misses: Int,
) : TieredParcelPreferenceStore {
    override fun find(code: String): TieredParcelPreference? {
        val hit = (cache.get(code) as TieredParcelPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredParcelPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredParcelPreference> = inner.list()
}

fun newCachedTieredParcelPreferenceStore(inner: TieredParcelPreferenceStore, ttl: Int, clock: Clock): TieredParcelPreferenceStore {
    return CachedTieredParcelPreferenceStore(
        inner = inner,
        cache = newTtlCache<TieredParcelPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered parcel preferences. Empty fields match all. */
data class TieredParcelPreferenceQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredParcelPreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredParcelPreference>): MutableList<TieredParcelPreference> {
        val matched = filter(items) { candidate: TieredParcelPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredParcelPreferences(store: TieredParcelPreferenceStore, query: TieredParcelPreferenceQuery): MutableList<TieredParcelPreference> {
    return query.apply(store.list())
}
