package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial preferences by code. */
interface AccessorialPreferenceStore {
    fun find(code: String): AccessorialPreference?
    fun save(item: AccessorialPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialPreferenceStore(
    var items: MutableMap<String, AccessorialPreference>,
    var order: MutableList<String>,
) : AccessorialPreferenceStore {
    override fun find(code: String): AccessorialPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialPreference) {
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

    override fun list(): MutableList<AccessorialPreference> {
        val out = mutableListOf<AccessorialPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialPreferenceStore(): AccessorialPreferenceStore {
    return MemoryAccessorialPreferenceStore(
        mutableMapOf<String, AccessorialPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialPreferenceStore(
    var inner: AccessorialPreferenceStore,
    var cache: TtlCache<AccessorialPreference>,
    var hits: Int,
    var misses: Int,
) : AccessorialPreferenceStore {
    override fun find(code: String): AccessorialPreference? {
        val hit = (cache.get(code) as AccessorialPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialPreference> = inner.list()
}

fun newCachedAccessorialPreferenceStore(inner: AccessorialPreferenceStore, ttl: Int, clock: Clock): AccessorialPreferenceStore {
    return CachedAccessorialPreferenceStore(
        inner = inner,
        cache = newTtlCache<AccessorialPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial preferences. Empty fields match all. */
data class AccessorialPreferenceQuery(
    var locale: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialPreference): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialPreference>): MutableList<AccessorialPreference> {
        val matched = filter(items) { candidate: AccessorialPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialPreferences(store: AccessorialPreferenceStore, query: AccessorialPreferenceQuery): MutableList<AccessorialPreference> {
    return query.apply(store.list())
}
