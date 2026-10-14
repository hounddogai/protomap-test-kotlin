package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient ev charge preferences by code. */
interface LenientEvChargePreferenceStore {
    fun find(code: String): LenientEvChargePreference?
    fun save(item: LenientEvChargePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientEvChargePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientEvChargePreferenceStore(
    var items: MutableMap<String, LenientEvChargePreference>,
    var order: MutableList<String>,
) : LenientEvChargePreferenceStore {
    override fun find(code: String): LenientEvChargePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientEvChargePreference) {
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

    override fun list(): MutableList<LenientEvChargePreference> {
        val out = mutableListOf<LenientEvChargePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientEvChargePreferenceStore(): LenientEvChargePreferenceStore {
    return MemoryLenientEvChargePreferenceStore(
        mutableMapOf<String, LenientEvChargePreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientEvChargePreferenceStore(
    var inner: LenientEvChargePreferenceStore,
    var cache: TtlCache<LenientEvChargePreference>,
    var hits: Int,
    var misses: Int,
) : LenientEvChargePreferenceStore {
    override fun find(code: String): LenientEvChargePreference? {
        val hit = (cache.get(code) as LenientEvChargePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientEvChargePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientEvChargePreference> = inner.list()
}

fun newCachedLenientEvChargePreferenceStore(inner: LenientEvChargePreferenceStore, ttl: Int, clock: Clock): LenientEvChargePreferenceStore {
    return CachedLenientEvChargePreferenceStore(
        inner = inner,
        cache = newTtlCache<LenientEvChargePreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient ev charge preferences. Empty fields match all. */
data class LenientEvChargePreferenceQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientEvChargePreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientEvChargePreference>): MutableList<LenientEvChargePreference> {
        val matched = filter(items) { candidate: LenientEvChargePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientEvChargePreferences(store: LenientEvChargePreferenceStore, query: LenientEvChargePreferenceQuery): MutableList<LenientEvChargePreference> {
    return query.apply(store.list())
}
