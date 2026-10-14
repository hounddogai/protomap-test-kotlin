package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global qualification preferences by code. */
interface GlobalQualificationPreferenceStore {
    fun find(code: String): GlobalQualificationPreference?
    fun save(item: GlobalQualificationPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalQualificationPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalQualificationPreferenceStore(
    var items: MutableMap<String, GlobalQualificationPreference>,
    var order: MutableList<String>,
) : GlobalQualificationPreferenceStore {
    override fun find(code: String): GlobalQualificationPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalQualificationPreference) {
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

    override fun list(): MutableList<GlobalQualificationPreference> {
        val out = mutableListOf<GlobalQualificationPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalQualificationPreferenceStore(): GlobalQualificationPreferenceStore {
    return MemoryGlobalQualificationPreferenceStore(
        mutableMapOf<String, GlobalQualificationPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalQualificationPreferenceStore(
    var inner: GlobalQualificationPreferenceStore,
    var cache: TtlCache<GlobalQualificationPreference>,
    var hits: Int,
    var misses: Int,
) : GlobalQualificationPreferenceStore {
    override fun find(code: String): GlobalQualificationPreference? {
        val hit = (cache.get(code) as GlobalQualificationPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalQualificationPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalQualificationPreference> = inner.list()
}

fun newCachedGlobalQualificationPreferenceStore(inner: GlobalQualificationPreferenceStore, ttl: Int, clock: Clock): GlobalQualificationPreferenceStore {
    return CachedGlobalQualificationPreferenceStore(
        inner = inner,
        cache = newTtlCache<GlobalQualificationPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global qualification preferences. Empty fields match all. */
data class GlobalQualificationPreferenceQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalQualificationPreference): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<GlobalQualificationPreference>): MutableList<GlobalQualificationPreference> {
        val matched = filter(items) { candidate: GlobalQualificationPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalQualificationPreferences(store: GlobalQualificationPreferenceStore, query: GlobalQualificationPreferenceQuery): MutableList<GlobalQualificationPreference> {
    return query.apply(store.list())
}
