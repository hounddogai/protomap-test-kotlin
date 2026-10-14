package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary redelivery preferences by code. */
interface PrimaryRedeliveryPreferenceStore {
    fun find(code: String): PrimaryRedeliveryPreference?
    fun save(item: PrimaryRedeliveryPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRedeliveryPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRedeliveryPreferenceStore(
    var items: MutableMap<String, PrimaryRedeliveryPreference>,
    var order: MutableList<String>,
) : PrimaryRedeliveryPreferenceStore {
    override fun find(code: String): PrimaryRedeliveryPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRedeliveryPreference) {
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

    override fun list(): MutableList<PrimaryRedeliveryPreference> {
        val out = mutableListOf<PrimaryRedeliveryPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRedeliveryPreferenceStore(): PrimaryRedeliveryPreferenceStore {
    return MemoryPrimaryRedeliveryPreferenceStore(
        mutableMapOf<String, PrimaryRedeliveryPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRedeliveryPreferenceStore(
    var inner: PrimaryRedeliveryPreferenceStore,
    var cache: TtlCache<PrimaryRedeliveryPreference>,
    var hits: Int,
    var misses: Int,
) : PrimaryRedeliveryPreferenceStore {
    override fun find(code: String): PrimaryRedeliveryPreference? {
        val hit = (cache.get(code) as PrimaryRedeliveryPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRedeliveryPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRedeliveryPreference> = inner.list()
}

fun newCachedPrimaryRedeliveryPreferenceStore(inner: PrimaryRedeliveryPreferenceStore, ttl: Int, clock: Clock): PrimaryRedeliveryPreferenceStore {
    return CachedPrimaryRedeliveryPreferenceStore(
        inner = inner,
        cache = newTtlCache<PrimaryRedeliveryPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary redelivery preferences. Empty fields match all. */
data class PrimaryRedeliveryPreferenceQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRedeliveryPreference): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryRedeliveryPreference>): MutableList<PrimaryRedeliveryPreference> {
        val matched = filter(items) { candidate: PrimaryRedeliveryPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRedeliveryPreferences(store: PrimaryRedeliveryPreferenceStore, query: PrimaryRedeliveryPreferenceQuery): MutableList<PrimaryRedeliveryPreference> {
    return query.apply(store.list())
}
