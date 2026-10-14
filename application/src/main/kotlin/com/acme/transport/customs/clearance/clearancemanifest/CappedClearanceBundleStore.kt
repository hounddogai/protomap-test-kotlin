package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped clearance bundles by code. */
interface CappedClearanceBundleStore {
    fun find(code: String): CappedClearanceBundle?
    fun save(item: CappedClearanceBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedClearanceBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedClearanceBundleStore(
    var items: MutableMap<String, CappedClearanceBundle>,
    var order: MutableList<String>,
) : CappedClearanceBundleStore {
    override fun find(code: String): CappedClearanceBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedClearanceBundle) {
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

    override fun list(): MutableList<CappedClearanceBundle> {
        val out = mutableListOf<CappedClearanceBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedClearanceBundleStore(): CappedClearanceBundleStore {
    return MemoryCappedClearanceBundleStore(
        mutableMapOf<String, CappedClearanceBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedClearanceBundleStore(
    var inner: CappedClearanceBundleStore,
    var cache: TtlCache<CappedClearanceBundle>,
    var hits: Int,
    var misses: Int,
) : CappedClearanceBundleStore {
    override fun find(code: String): CappedClearanceBundle? {
        val hit = (cache.get(code) as CappedClearanceBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedClearanceBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedClearanceBundle> = inner.list()
}

fun newCachedCappedClearanceBundleStore(inner: CappedClearanceBundleStore, ttl: Int, clock: Clock): CappedClearanceBundleStore {
    return CachedCappedClearanceBundleStore(
        inner = inner,
        cache = newTtlCache<CappedClearanceBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped clearance bundles. Empty fields match all. */
data class CappedClearanceBundleQuery(
    var locale: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedClearanceBundle): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedClearanceBundle>): MutableList<CappedClearanceBundle> {
        val matched = filter(items) { candidate: CappedClearanceBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedClearanceBundles(store: CappedClearanceBundleStore, query: CappedClearanceBundleQuery): MutableList<CappedClearanceBundle> {
    return query.apply(store.list())
}
