package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled redelivery preferences by code. */
interface ScheduledRedeliveryPreferenceStore {
    fun find(code: String): ScheduledRedeliveryPreference?
    fun save(item: ScheduledRedeliveryPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRedeliveryPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRedeliveryPreferenceStore(
    var items: MutableMap<String, ScheduledRedeliveryPreference>,
    var order: MutableList<String>,
) : ScheduledRedeliveryPreferenceStore {
    override fun find(code: String): ScheduledRedeliveryPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRedeliveryPreference) {
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

    override fun list(): MutableList<ScheduledRedeliveryPreference> {
        val out = mutableListOf<ScheduledRedeliveryPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRedeliveryPreferenceStore(): ScheduledRedeliveryPreferenceStore {
    return MemoryScheduledRedeliveryPreferenceStore(
        mutableMapOf<String, ScheduledRedeliveryPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRedeliveryPreferenceStore(
    var inner: ScheduledRedeliveryPreferenceStore,
    var cache: TtlCache<ScheduledRedeliveryPreference>,
    var hits: Int,
    var misses: Int,
) : ScheduledRedeliveryPreferenceStore {
    override fun find(code: String): ScheduledRedeliveryPreference? {
        val hit = (cache.get(code) as ScheduledRedeliveryPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRedeliveryPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRedeliveryPreference> = inner.list()
}

fun newCachedScheduledRedeliveryPreferenceStore(inner: ScheduledRedeliveryPreferenceStore, ttl: Int, clock: Clock): ScheduledRedeliveryPreferenceStore {
    return CachedScheduledRedeliveryPreferenceStore(
        inner = inner,
        cache = newTtlCache<ScheduledRedeliveryPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled redelivery preferences. Empty fields match all. */
data class ScheduledRedeliveryPreferenceQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRedeliveryPreference): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRedeliveryPreference>): MutableList<ScheduledRedeliveryPreference> {
        val matched = filter(items) { candidate: ScheduledRedeliveryPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRedeliveryPreferences(store: ScheduledRedeliveryPreferenceStore, query: ScheduledRedeliveryPreferenceQuery): MutableList<ScheduledRedeliveryPreference> {
    return query.apply(store.list())
}
