package com.acme.transport.safety.incident.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident bundles by code. */
interface IncidentBundleStore {
    fun find(code: String): IncidentBundle?
    fun save(item: IncidentBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentBundleStore(
    var items: MutableMap<String, IncidentBundle>,
    var order: MutableList<String>,
) : IncidentBundleStore {
    override fun find(code: String): IncidentBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentBundle) {
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

    override fun list(): MutableList<IncidentBundle> {
        val out = mutableListOf<IncidentBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentBundleStore(): IncidentBundleStore {
    return MemoryIncidentBundleStore(mutableMapOf<String, IncidentBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentBundleStore(
    var inner: IncidentBundleStore,
    var cache: TtlCache<IncidentBundle>,
    var hits: Int,
    var misses: Int,
) : IncidentBundleStore {
    override fun find(code: String): IncidentBundle? {
        val hit = (cache.get(code) as IncidentBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentBundle> = inner.list()
}

fun newCachedIncidentBundleStore(inner: IncidentBundleStore, ttl: Int, clock: Clock): IncidentBundleStore {
    return CachedIncidentBundleStore(
        inner = inner,
        cache = newTtlCache<IncidentBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident bundles. Empty fields match all. */
data class IncidentBundleQuery(
    var locale: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentBundle): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentBundle>): MutableList<IncidentBundle> {
        val matched = filter(items) { candidate: IncidentBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentBundles(store: IncidentBundleStore, query: IncidentBundleQuery): MutableList<IncidentBundle> {
    return query.apply(store.list())
}
