package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor bundles by code. */
interface EmissionFactorBundleStore {
    fun find(code: String): EmissionFactorBundle?
    fun save(item: EmissionFactorBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorBundleStore(
    var items: MutableMap<String, EmissionFactorBundle>,
    var order: MutableList<String>,
) : EmissionFactorBundleStore {
    override fun find(code: String): EmissionFactorBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorBundle) {
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

    override fun list(): MutableList<EmissionFactorBundle> {
        val out = mutableListOf<EmissionFactorBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorBundleStore(): EmissionFactorBundleStore {
    return MemoryEmissionFactorBundleStore(
        mutableMapOf<String, EmissionFactorBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorBundleStore(
    var inner: EmissionFactorBundleStore,
    var cache: TtlCache<EmissionFactorBundle>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorBundleStore {
    override fun find(code: String): EmissionFactorBundle? {
        val hit = (cache.get(code) as EmissionFactorBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorBundle> = inner.list()
}

fun newCachedEmissionFactorBundleStore(inner: EmissionFactorBundleStore, ttl: Int, clock: Clock): EmissionFactorBundleStore {
    return CachedEmissionFactorBundleStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor bundles. Empty fields match all. */
data class EmissionFactorBundleQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorBundle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorBundle>): MutableList<EmissionFactorBundle> {
        val matched = filter(items) { candidate: EmissionFactorBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorBundles(store: EmissionFactorBundleStore, query: EmissionFactorBundleQuery): MutableList<EmissionFactorBundle> {
    return query.apply(store.list())
}
