package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop preferences by code. */
interface DropPreferenceStore {
    fun find(code: String): DropPreference?
    fun save(item: DropPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropPreferenceStore(
    var items: MutableMap<String, DropPreference>,
    var order: MutableList<String>,
) : DropPreferenceStore {
    override fun find(code: String): DropPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropPreference) {
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

    override fun list(): MutableList<DropPreference> {
        val out = mutableListOf<DropPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropPreferenceStore(): DropPreferenceStore {
    return MemoryDropPreferenceStore(mutableMapOf<String, DropPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropPreferenceStore(
    var inner: DropPreferenceStore,
    var cache: TtlCache<DropPreference>,
    var hits: Int,
    var misses: Int,
) : DropPreferenceStore {
    override fun find(code: String): DropPreference? {
        val hit = (cache.get(code) as DropPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropPreference> = inner.list()
}

fun newCachedDropPreferenceStore(inner: DropPreferenceStore, ttl: Int, clock: Clock): DropPreferenceStore {
    return CachedDropPreferenceStore(
        inner = inner,
        cache = newTtlCache<DropPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop preferences. Empty fields match all. */
data class DropPreferenceQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropPreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DropPreference>): MutableList<DropPreference> {
        val matched = filter(items) { candidate: DropPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropPreferences(store: DropPreferenceStore, query: DropPreferenceQuery): MutableList<DropPreference> {
    return query.apply(store.list())
}
