package com.acme.transport.customs.declaration.preference

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration preferences by code. */
interface DeclarationPreferenceStore {
    fun find(code: String): DeclarationPreference?
    fun save(item: DeclarationPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationPreferenceStore(
    var items: MutableMap<String, DeclarationPreference>,
    var order: MutableList<String>,
) : DeclarationPreferenceStore {
    override fun find(code: String): DeclarationPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationPreference) {
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

    override fun list(): MutableList<DeclarationPreference> {
        val out = mutableListOf<DeclarationPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationPreferenceStore(): DeclarationPreferenceStore {
    return MemoryDeclarationPreferenceStore(
        mutableMapOf<String, DeclarationPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationPreferenceStore(
    var inner: DeclarationPreferenceStore,
    var cache: TtlCache<DeclarationPreference>,
    var hits: Int,
    var misses: Int,
) : DeclarationPreferenceStore {
    override fun find(code: String): DeclarationPreference? {
        val hit = (cache.get(code) as DeclarationPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationPreference> = inner.list()
}

fun newCachedDeclarationPreferenceStore(inner: DeclarationPreferenceStore, ttl: Int, clock: Clock): DeclarationPreferenceStore {
    return CachedDeclarationPreferenceStore(
        inner = inner,
        cache = newTtlCache<DeclarationPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration preferences. Empty fields match all. */
data class DeclarationPreferenceQuery(
    var category: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationPreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationPreference>): MutableList<DeclarationPreference> {
        val matched = filter(items) { candidate: DeclarationPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationPreferences(store: DeclarationPreferenceStore, query: DeclarationPreferenceQuery): MutableList<DeclarationPreference> {
    return query.apply(store.list())
}
